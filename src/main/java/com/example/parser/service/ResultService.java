package com.example.parser.service;

import com.example.parser.calculator.BonusCalculator;
import com.example.parser.calculator.PlacementCalculator;
import com.example.parser.calculator.PointsCalculator;
import com.example.parser.calculator.PointsCalculatorFactory;
import com.example.parser.dto.ResultDto;
import com.example.parser.entity.Player;
import com.example.parser.entity.TournamentResultEntity;
import com.example.parser.model.LeagueType;
import com.example.parser.model.Match;
import com.example.parser.model.Result;
import com.example.parser.parser.MatchNormalizer;
import com.example.parser.parser.MatchParser;
import com.example.parser.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ResultService {

    private final MatchParser parser;
    private final MatchNormalizer normalizer;
    private final PlacementCalculator placementCalculator;
    private final BonusCalculator bonusCalculator;
    private final LeagueDetector leagueDetector;
    private final PointsCalculatorFactory factory;

    public ParsedResult calculateAll(String url) throws Exception {

        LeagueType league = leagueDetector.detectLeague(url);
        PointsCalculator pointsCalculator = factory.getCalculator(league);

        MatchParser.ParsedTournament parsed = parser.parseMatches(url);

        Long tournamentId = parsed.getTournamentId();
        var matches = parsed.getMatches();
        boolean finished = parsed.isFinished();

        Map<String, Integer> pointsMap = new HashMap<>();
        Map<String, Integer> placeMap = new HashMap<>();

        String dateText = parser.parseDate(url);

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

        return new ParsedResult(tournamentId, results, finished);
    }

    private String normalize(String name) {
        if (name == null) return "";
        return name.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    // 🔥 ВАЖНЫЙ КЛАСС
    public static class ParsedResult {
        private Long tournamentId;
        private List<ResultDto> results;
        private boolean finished;

        public ParsedResult(Long tournamentId, List<ResultDto> results, boolean finished) {
            this.tournamentId = tournamentId;
            this.results = results;
            this.finished = finished;
        }

        public Long getTournamentId() {
            return tournamentId;
        }

        public List<ResultDto> getResults() {
            return results;
        }

        public boolean isFinished() {
            return finished;
        }
    }
}