package com.example.parser.lineup;

import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.domain.entity.Lineup;
import com.example.parser.repository.LineupRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LineupService {

    private final LineupRepository lineupRepository;
    private final ObjectMapper mapper;

    private static final String URL = "https://masters-league.com/wp-admin/admin-ajax.php";

    @Transactional
    public void loadLineups() {

        try {
            log.info("🚀 Loading lineups...");

            Connection.Response res = Jsoup.connect(URL)
                    .method(Connection.Method.POST)
                    .header("User-Agent", "Mozilla/5.0")
                    .data("action", "tourslist")
                    .data("country", "RUS")
                    .ignoreContentType(true)
                    .timeout(10000)
                    .execute();

            String body = res.body();

            List<TournamentDto> tournaments = mapper.readValue(
                    body,
                    new TypeReference<List<TournamentDto>>() {}
            );

            if (tournaments == null || tournaments.isEmpty()) {
                log.warn("⚠️ API пустой — ничего не делаем");
                return;
            }

            // 🔥 определяем дату из API
            LocalDate apiDate = extractDate(tournaments.get(0));

            if (apiDate == null) {
                log.warn("⚠️ не удалось определить дату");
                return;
            }

            // 🔥 дата в базе
            LocalDate dbDate = lineupRepository.findMaxDate();

            log.info("📅 API date: {}, DB date: {}", apiDate, dbDate);

            // =====================================================
            // 🔥 ЕСЛИ НОВЫЙ ДЕНЬ — ПОЛНАЯ ЗАМЕНА
            // =====================================================
            if (dbDate == null || apiDate.isAfter(dbDate)) {

                log.info("🔥 Новый день — удаляем всё и записываем заново");

                lineupRepository.deleteAll();

                List<Lineup> newLineups = tournaments.stream()
                        .filter(this::isValidTournament)
                        .map(t -> mapToEntity(t, apiDate))
                        .toList();

                lineupRepository.saveAll(newLineups);

                log.info("✅ Saved new lineups: {}", newLineups.size());

                return;
            }

            // =====================================================
            // 🔥 ЕСЛИ ТОТ ЖЕ ДЕНЬ — ОБНОВЛЯЕМ
            // =====================================================
            int created = 0;
            int updated = 0;

            for (TournamentDto t : tournaments) {

                if (!isValidTournament(t)) continue;

                String time = extractTime(t);
                String players = String.join(", ", t.getPlayers());

                Optional<Lineup> existing = lineupRepository
                        .findByLeagueAndTimeAndDate(
                                t.getLeague(),
                                time,
                                apiDate
                        );

                if (existing.isPresent()) {

                    Lineup lineup = existing.get();

                    if (!players.equals(lineup.getPlayers())) {
                        lineup.setPlayers(players);
                        lineupRepository.save(lineup);
                        updated++;
                    }

                } else {

                    Lineup lineup = mapToEntity(t, apiDate);
                    lineupRepository.save(lineup);
                    created++;
                }
            }

            log.info("✅ Updated: {}, Created: {}", updated, created);

        } catch (Exception e) {
            log.error("❌ Error loading lineups", e);
        }
    }

    // =========================
    // helpers
    // =========================

    private boolean isValidTournament(TournamentDto t) {
        Integer hallNumber = extractHallNumber(t.getHall());

        return hallNumber != null
                && (hallNumber == 10 || hallNumber == 11)
                && t.getPlayers() != null
                && !t.getPlayers().isEmpty();
    }

    private LocalDate extractDate(TournamentDto t) {
        try {
            String full = t.getDate().getDate(); // "2026-04-16 10:00:00"
            return LocalDate.parse(full.substring(0, 10));
        } catch (Exception e) {
            return null;
        }
    }

    private String extractTime(TournamentDto t) {
        try {
            String full = t.getDate().getDate();
            return full.substring(11, 16);
        } catch (Exception e) {
            return "??:??";
        }
    }

    private Integer extractHallNumber(String hall) {
        if (hall == null) return null;

        try {
            String digits = hall.replaceAll("\\D+", "");
            return digits.isEmpty() ? null : Integer.parseInt(digits);
        } catch (Exception e) {
            return null;
        }
    }

    private Lineup mapToEntity(TournamentDto t, LocalDate date) {

        return Lineup.builder()
                .league(t.getLeague())
                .time(extractTime(t))
                .players(String.join(", ", t.getPlayers()))
                .date(date)
                .city("Ростов")
                .build();
    }
}