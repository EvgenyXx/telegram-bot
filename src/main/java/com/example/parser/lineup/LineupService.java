package com.example.parser.lineup;

import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.domain.entity.Lineup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LineupService {

    private final LineupRepository lineupRepository;
    private final MastersApiClient apiClient;
    private final LineupMapper mapper;
    private final TournamentValidator validator;

    @Transactional
    public void loadLineups() {
        try {
            log.info("🚀 Loading lineups...");

            List<TournamentDto> tournaments = apiClient.loadTournaments();

            if (tournaments.isEmpty()) {
                log.warn("⚠️ API пустой");
                return;
            }

            // 🔥 целевая дата = завтра
            LocalDate targetDate = LocalDate.now().plusDays(1);

            // 🔥 берём только турниры на завтра
            List<TournamentDto> filtered = tournaments.stream()
                    .filter(t -> targetDate.equals(extractDate(t)))
                    .toList();

            if (filtered.isEmpty()) {
                log.warn("⚠️ Нет составов на завтра");
                return;
            }

            LocalDate dbDate = lineupRepository.findMaxDate();

            // 🔥 если в базе не завтра → полностью заменяем
            if (dbDate == null || !dbDate.equals(targetDate)) {
                handleNewDay(filtered, targetDate);
            } else {
                // 🔥 иначе обновляем
                handleSameDay(filtered, targetDate);
            }

        } catch (Exception e) {
            log.error("❌ Error loading lineups", e);
        }
    }

    private void handleNewDay(List<TournamentDto> tournaments, LocalDate date) {
        log.info("🆕 Новый день → полная замена данных");

        lineupRepository.deleteAll();

        List<Lineup> list = tournaments.stream()
                .filter(validator::isValid)
                .map(t -> mapper.toEntity(t, date, extractTime(t)))
                .toList();

        lineupRepository.saveAll(list);
    }

    private void handleSameDay(List<TournamentDto> tournaments, LocalDate date) {
        log.info("🔄 Обновление текущего дня");

        for (TournamentDto t : tournaments) {

            if (!validator.isValid(t)) continue;

            String time = extractTime(t);
            String players = String.join(", ", t.getPlayers());

            Lineup lineup = lineupRepository
                    .findByLeagueAndTimeAndDate(t.getLeague(), time, date)
                    .orElseGet(() -> mapper.toEntity(t, date, time));

            // обновляем только если изменились игроки
            if (!players.equals(lineup.getPlayers())) {
                lineup.setPlayers(players);
            }

            lineupRepository.save(lineup);
        }
    }

    private LocalDate extractDate(TournamentDto t) {
        try {
            return LocalDate.parse(t.getDate().getDate().substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }

    private String extractTime(TournamentDto t) {
        try {
            return t.getDate().getDate().substring(11, 16);
        } catch (Exception e) {
            return "??:??";
        }
    }
}