package com.example.parser.service;

import com.example.parser.calculator.*;
import com.example.parser.domain.dto.ResultDto;
import com.example.parser.integration.DocumentLoader;
import com.example.parser.domain.model.LeagueType;
import com.example.parser.domain.model.Match;
import com.example.parser.parser.LeagueDetector;
import com.example.parser.parser.MatchParser;
import com.example.parser.parser.TournamentParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final DocumentLoader loader;
    private final MatchParser matchParser;
    private final TournamentParser tournamentParser;
    private final PlacementCalculator placementCalculator;
    private final BonusCalculator bonusCalculator;
    private final LeagueDetector leagueDetector;
    private final PointsCalculatorFactory factory;
    private final NightBonusService nightBonusService;

    public ParsedResult calculateAll(String url) throws Exception {

        // 1. грузим документ
        Document doc = loader.load(url);

        // 2. парсим турнир
        Long tournamentId = tournamentParser.parseTournamentId(doc);
        boolean finished = tournamentParser.isFinished(doc);
        String dateText = tournamentParser.parseDate(doc);

        // 3. парсим матчи
        List<Match> matches = matchParser.parseMatches(doc);

        // 4. определяем лигу
        LeagueType league = leagueDetector.detectLeague(doc);

        // 5. ночной бонус
        double nightBonus = nightBonusService.calculateBonus(doc, league.name());

        // 6. калькулятор очков
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

        // финальный результат
        ParsedResult result = new ParsedResult(
                tournamentId,
                results,
                finished,
                nightBonus
        );

        result.setLeague(league.name());

        return result;
    }

    private String normalize(String name) {
        if (name == null) return "";
        return name.toLowerCase().trim().replaceAll("\\s+", " ");
    }

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