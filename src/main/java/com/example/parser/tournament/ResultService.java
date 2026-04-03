package com.example.parser.tournament;

import com.example.parser.domain.dto.ResultDto;
import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.integration.DocumentLoader;
import com.example.parser.domain.model.LeagueType;
import com.example.parser.domain.model.Match;
import com.example.parser.parser.LeagueDetector;
import com.example.parser.stats.NightBonusService;
import com.example.parser.stats.BonusCalculator;
import com.example.parser.stats.PlacementCalculator;
import com.example.parser.stats.PointsCalculator;
import com.example.parser.stats.PointsCalculatorFactory;
import com.example.parser.tournament.parser.MatchParser;
import com.example.parser.tournament.parser.TournamentParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResultService {

    private final DocumentLoader loader;
    private final MatchParser matchParser;
    private final TournamentParser tournamentParser;
    private final PlacementCalculator placementCalculator;
    private final BonusCalculator bonusCalculator;
    private final LeagueDetector leagueDetector;
    private final PointsCalculatorFactory factory;
    private final NightBonusService nightBonusService;

    // =========================
    // ОСНОВНОЙ ПАРСИНГ ТУРНИРА
    // =========================
    public ParsedResult calculateAll(String url) throws Exception {

        log.warn("🔄 [CALC] Начинаем парсинг: {}", url);

        Document doc = loader.load(url);
        log.warn("🌐 [PARSER] Документ загружен");

        Long tournamentId = tournamentParser.parseTournamentId(doc);
        boolean finished = tournamentParser.isFinished(doc);
        String dateText = tournamentParser.parseDate(doc);

        List<Match> matches = matchParser.parseMatches(doc);
        log.warn("⚔️ [PARSER] Матчей найдено: {}", matches.size());

        LeagueType league = leagueDetector.detectLeague(doc);
        double nightBonus = nightBonusService.calculateBonus(doc, league.name());

        PointsCalculator pointsCalculator = factory.getCalculator(league);

        Map<String, Integer> pointsMap = new HashMap<>();
        Map<String, Integer> placeMap = new HashMap<>();

        for (Match m : matches) {
            String p1 = normalize(m.getPlayer1());
            String p2 = normalize(m.getPlayer2());

            int points1 = pointsCalculator.calculatePoints(m);
            pointsMap.merge(p1, points1, Integer::sum);

            int place1 = placementCalculator.calculatePlace(m);
            if (place1 != 0) placeMap.put(p1, place1);

            Match reversed = m.reverse();

            int points2 = pointsCalculator.calculatePoints(reversed);
            pointsMap.merge(p2, points2, Integer::sum);

            int place2 = placementCalculator.calculatePlace(reversed);
            if (place2 != 0) placeMap.put(p2, place2);
        }

        List<ResultDto> results = new ArrayList<>();

        for (String player : pointsMap.keySet()) {
            int place = placeMap.getOrDefault(player, 0);
            int bonus = bonusCalculator.getBonus(place);
            int total = pointsMap.get(player) + bonus;

            results.add(new ResultDto(player, place, bonus, total, dateText));
        }

        results.sort((a, b) -> Integer.compare(b.getTotal(), a.getTotal()));

        log.warn("🏆 [CALC] Игроков посчитано: {}", results.size());
        log.warn("🏁 [CALC] finished={}", finished);
        log.warn("🆔 [CALC] tournamentId={}", tournamentId);

        return new ParsedResult(tournamentId, results, finished, nightBonus);
    }

    // =========================
    // 🔥 ПРОВЕРКА БЛИЖАЙШИХ ТУРНИРОВ
    // =========================
    public boolean isPlayerInUpcoming(String searchName) {
        log.info("🔍 Поиск игрока в ближайших турнирах: [{}]", searchName);

        try {
            String url = "https://masters-league.com/wp-admin/admin-ajax.php";

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            for (int i = 0; i <= 2; i++) {
                String date = LocalDate.now().plusDays(i).toString();

                log.debug("📅 Проверяем дату: {}", date);

                Connection.Response res = Jsoup.connect(url)
                        .method(Connection.Method.POST)
                        .header("User-Agent", "Mozilla/5.0")
                        .data("action", "tourslist")
                        .data("date", date)
                        .data("country", "RUS")
                        .ignoreContentType(true)
                        .timeout(10000)
                        .execute();

                String json = res.body();

                List<TournamentDto> tournaments = mapper.readValue(
                        json,
                        new TypeReference<List<TournamentDto>>() {}
                );

                for (TournamentDto t : tournaments) {
                    if (t.getPlayers() == null) continue;

                    for (String player : t.getPlayers()) {
                        if (player == null) continue;

                        if (isSamePlayer(searchName, player)) {
                            log.info("✅ Найден игрок [{}] в турнире id={}", searchName, t.getId());
                            return true;
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("❌ Ошибка при поиске игрока [{}]: {}", searchName, e.getMessage(), e);
        }

        log.info("❌ Игрок [{}] не найден в ближайших турнирах", searchName);
        return false;
    }

    // =========================
    // СРАВНЕНИЕ ИМЁН
    // =========================
    private boolean isSamePlayer(String n1, String n2) {
        return normalize(n1).equals(normalize(n2));
    }

    private String normalize(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    // =========================
    // DTO
    // =========================
    @Getter
    @Setter
    public static class ParsedResult {
        private Long tournamentId;
        private List<ResultDto> results;
        private boolean finished;
        private String league;
        private double nightBonus;

        public ParsedResult(Long tournamentId,
                            List<ResultDto> results,
                            boolean finished,
                            double nightBonus) {
            this.tournamentId = tournamentId;
            this.results = results;
            this.finished = finished;
            this.nightBonus = nightBonus;
        }
    }
}