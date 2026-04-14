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

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LineupService {

    private final LineupRepository lineupRepository;
    private final ObjectMapper mapper; // ✅ правильный mapper от Spring

    private static final String URL = "https://masters-league.com/wp-admin/admin-ajax.php";

    public void loadLineupsForTomorrow() {

        LocalDate targetDate = LocalDate.now().plusDays(1);

        try {
            log.info("🚀 Loading lineups for {}", targetDate);

            Connection.Response res = Jsoup.connect(URL)
                    .method(Connection.Method.POST)
                    .header("User-Agent", "Mozilla/5.0")
                    .data("action", "tourslist")
                    .data("date", targetDate.toString())
                    .data("country", "RUS")
                    .ignoreContentType(true)
                    .timeout(10000)
                    .execute();

            String body = res.body();

            log.debug("📦 Raw response: {}", body);

            List<TournamentDto> tournaments = mapper.readValue(
                    body,
                    new TypeReference<List<TournamentDto>>() {}
            );

            int created = 0;
            int updated = 0;

            for (TournamentDto t : tournaments) {

                Integer hallNumber = extractHallNumber(t.getHall());

                // 🔥 фильтр столов
                if (hallNumber == null || (hallNumber != 10 && hallNumber != 11)) {
                    continue;
                }

                if (t.getPlayers() == null || t.getPlayers().isEmpty()) {
                    continue;
                }

                String time = extractTime(t);
                String players = String.join(", ", t.getPlayers());

                var existing = lineupRepository.findByLeagueAndTimeAndDate(
                        t.getLeague(),
                        time,
                        targetDate
                );

                if (existing.isPresent()) {
                    Lineup lineup = existing.get();

                    if (!players.equals(lineup.getPlayers())) {
                        lineup.setPlayers(players);
                        lineupRepository.save(lineup);
                        updated++;

                        log.debug("🔄 Updated lineup: {} {}", t.getLeague(), time);
                    }

                } else {
                    Lineup lineup = Lineup.builder()
                            .league(t.getLeague())
                            .time(time)
                            .players(players)
                            .date(targetDate)
                            .city("Ростов")
                            .build();

                    lineupRepository.save(lineup);
                    created++;

                    log.debug("➕ Created lineup: {} {}", t.getLeague(), time);
                }
            }

            log.info("✅ Done. Created: {}, Updated: {}", created, updated);

        } catch (Exception e) {
            log.error("❌ Error loading lineups", e);
        }
    }

    // =========================
    // helpers
    // =========================

    private String extractTime(TournamentDto t) {
        try {
            if (t.getDate() == null || t.getDate().getDate() == null) {
                return "??:??";
            }

            String full = t.getDate().getDate(); // "2026-04-15 10:00:00"

            if (full.length() >= 16) {
                return full.substring(11, 16);
            }

            return "??:??";

        } catch (Exception e) {
            log.warn("⚠️ Failed to extract time from: {}", t, e);
            return "??:??";
        }
    }

    private Integer extractHallNumber(String hall) {
        if (hall == null) return null;

        try {
            String digits = hall.replaceAll("\\D+", "");
            return digits.isEmpty() ? null : Integer.parseInt(digits);

        } catch (Exception e) {
            log.warn("⚠️ Failed to extract hall from: {}", hall, e);
            return null;
        }
    }
}