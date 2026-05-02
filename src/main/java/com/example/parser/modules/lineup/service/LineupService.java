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
    public void cleanupOld() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        lineupRepository.deleteByDateBefore(yesterday);
        log.info("Cleaned lineup records before {}", yesterday);
    }


    @Transactional
    public void loadLineups() {
        try {
            log.info("🚀 Loading lineups...");

            LocalDate today = LocalDate.now();

            for (int i = 1; i <= 2; i++) {
                LocalDate targetDate = today.plusDays(i);
                loadDay(targetDate);
            }

        } catch (Exception e) {
            log.error("❌ Error loading lineups", e);
        }
    }

    private void loadDay(LocalDate targetDate) {
        List<TournamentDto> tournaments = apiClient.loadTournaments(targetDate.toString());

        if (tournaments.isEmpty()) {
            log.warn("⚠️ API пустой для даты {}", targetDate);
            return;
        }

        List<TournamentDto> filtered = tournaments.stream()
                .filter(t -> targetDate.equals(extractDate(t)))
                .toList();

        if (filtered.isEmpty()) {
            log.warn("⚠️ Нет составов на {}", targetDate);
            return;
        }

        List<Lineup> existing = lineupRepository.findByDate(targetDate);

        if (existing.isEmpty()) {
            List<Lineup> list = filtered.stream()
                    .filter(validator::isValid)
                    .map(t -> mapper.toEntity(t, targetDate, extractTime(t)))
                    .toList();

            lineupRepository.saveAll(list);
            log.info("✅ Сохранено {} составов на {}", list.size(), targetDate);
        } else {
            handleSameDay(filtered, targetDate);
        }
    }

    private void handleSameDay(List<TournamentDto> tournaments, LocalDate date) {
        List<TournamentDto> valid = tournaments.stream()
                .filter(validator::isValid)
                .toList();

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