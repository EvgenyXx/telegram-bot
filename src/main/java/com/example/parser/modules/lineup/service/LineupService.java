package com.example.parser.modules.lineup.service;

import com.example.parser.core.dto.TournamentDto;
import com.example.parser.modules.lineup.domain.Lineup;
import com.example.parser.modules.lineup.mapper.LineupMapper;
import com.example.parser.modules.lineup.repository.LineupRepository;
import com.example.parser.modules.lineup.client.MastersApiClient;
import com.example.parser.modules.lineup.validator.TournamentValidator;
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

            LocalDate targetDate = LocalDate.now().plusDays(1);

            List<TournamentDto> tournaments =
                    apiClient.loadTournaments(targetDate.toString());

            if (tournaments.isEmpty()) {
                log.warn("⚠️ API пустой для даты {}", targetDate);
                return;
            }

            List<TournamentDto> filtered = tournaments.stream()
                    .filter(t -> targetDate.equals(extractDate(t)))
                    .toList();

            if (filtered.isEmpty()) {
                log.warn("⚠️ Нет составов на завтра {}", targetDate);
                return;
            }

            LocalDate dbDate = lineupRepository.findMaxDate();

            if (dbDate == null || !dbDate.equals(targetDate)) {
                handleNewDay(filtered, targetDate);
            } else {
                handleSameDay(filtered, targetDate);
            }

        } catch (Exception e) {
            log.error("❌ Error loading lineups", e);
        }
    }

    private void handleNewDay(List<TournamentDto> tournaments, LocalDate date) {


        List<Lineup> list = tournaments.stream()
                .filter(validator::isValid)
                .map(t -> mapper.toEntity(t, date, extractTime(t)))
                .toList();

        if (list.size() <= 1) {
            log.warn("⚠️ Новый день, но пришел 1 состав — НЕ трогаем базу");
            return;
        }



        lineupRepository.deleteAll();
        lineupRepository.saveAll(list);
    }

    private void handleSameDay(List<TournamentDto> tournaments, LocalDate date) {


        List<TournamentDto> valid = tournaments.stream()
                .filter(validator::isValid)
                .toList();

        if (valid.size() <= 1) {
            log.warn("⚠️ Пришел только 1 состав — НЕ обновляем");
            return;
        }

        for (TournamentDto t : valid) {
            String time = extractTime(t);
            String players = String.join(", ", t.getPlayers());

            Lineup lineup = lineupRepository
                    .findByLeagueAndTimeAndDate(t.getLeague(), time, date)
                    .orElseGet(() -> mapper.toEntity(t, date, time));

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