package com.example.parser;

import com.example.parser.dto.ResultDto;
import com.example.parser.dto.TournamentResult;
import com.example.parser.service.LeagueDetector;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResultService {

    private final MatchParser parser;
    private final MatchNormalizer normalizer;
    private final PlacementCalculator placementCalculator;
    private final BonusCalculator bonusCalculator;
    private final LeagueDetector leagueDetector;
    private final PointsCalculatorFactory factory;

    public ResultService(MatchParser parser,
                         MatchNormalizer normalizer,
                         PlacementCalculator placementCalculator,
                         BonusCalculator bonusCalculator,
                         LeagueDetector leagueDetector,
                         PointsCalculatorFactory factory) {
        this.parser = parser;
        this.normalizer = normalizer;
        this.placementCalculator = placementCalculator;
        this.bonusCalculator = bonusCalculator;
        this.leagueDetector = leagueDetector;
        this.factory = factory;
    }

    public Result calculate(String url, String targetPlayer) throws Exception {

        String normalizedTarget = normalize(targetPlayer);

        System.out.println("=== START CALCULATION ===");
        System.out.println("URL: " + url);
        System.out.println("TARGET RAW: " + targetPlayer);
        System.out.println("TARGET NORMALIZED: " + normalizedTarget);

        LeagueType league = leagueDetector.detectLeague(url);
        System.out.println("DETECTED LEAGUE: " + league);

        PointsCalculator pointsCalculator = factory.getCalculator(league);

        var matches = parser.parseMatches(url);
        System.out.println("TOTAL MATCHES PARSED: " + matches.size());

        int total = 0;
        int place = 0;

        for (Match m : matches) {

            System.out.println("----- MATCH START -----");

            m = normalizer.normalize(m, targetPlayer);

            String p1 = normalize(m.getPlayer1());
            String p2 = normalize(m.getPlayer2());

            System.out.println("STAGE: " + m.getStage());
            System.out.println("P1: " + p1 + " SCORE1: " + m.getScore1());
            System.out.println("P2: " + p2 + " SCORE2: " + m.getScore2());

            if (!p1.contains(normalizedTarget)) {
                System.out.println("SKIPPED (not target player)");
                continue;
            }

            System.out.println("MATCH ACCEPTED");

            int points = pointsCalculator.calculatePoints(m);
            System.out.println("POINTS: " + points);

            total += points;

            int p = placementCalculator.calculatePlace(m);
            System.out.println("PLACE FROM MATCH: " + p);

            if (p != 0) {
                place = p;
                System.out.println("PLACE UPDATED TO: " + place);
            }

            System.out.println("----- MATCH END -----");
        }

        int bonus = bonusCalculator.getBonus(place);

        System.out.println("FINAL PLACE: " + place);
        System.out.println("BONUS: " + bonus);
        System.out.println("TOTAL BEFORE BONUS: " + total);

        total += bonus;

        System.out.println("TOTAL WITH BONUS: " + total);
        System.out.println("=== END CALCULATION ===");

        return new Result(place, bonus, total);
    }

    private String normalize(String name) {
        if (name == null) return "";
        return name.toLowerCase().trim().replaceAll("\\s+", " ");
    }


    public List<ResultDto> calculateAll(String url) throws Exception {



        LeagueType league = leagueDetector.detectLeague(url);
        PointsCalculator pointsCalculator = factory.getCalculator(league);

        var matches = parser.parseMatches(url);

        Map<String, Integer> pointsMap = new HashMap<>();
        Map<String, Integer> placeMap = new HashMap<>();

        for (Match m : matches) {

            String p1 = normalize(m.getPlayer1());
            String p2 = normalize(m.getPlayer2());



            // --- PLAYER 1 ---
            int points1 = pointsCalculator.calculatePoints(m);
            pointsMap.merge(p1, points1, Integer::sum);

            int place1 = placementCalculator.calculatePlace(m);
            if (place1 != 0) {
                placeMap.put(p1, place1);
            }

            // --- PLAYER 2 ---
            Match reversed = m.reverse();

            int points2 = pointsCalculator.calculatePoints(reversed);
            pointsMap.merge(p2, points2, Integer::sum);

            int place2 = placementCalculator.calculatePlace(reversed);
            if (place2 != 0) {
                placeMap.put(p2, place2);
            }
        }

        List<ResultDto> results = new ArrayList<>();

        for (String player : pointsMap.keySet()) {

            int place = placeMap.getOrDefault(player, 0);
            int bonus = bonusCalculator.getBonus(place);
            int total = pointsMap.get(player) + bonus;



            results.add(new ResultDto(player, place, bonus, total));
        }

        // сортировка по убыванию
        results.sort((a, b) -> Integer.compare(b.getTotal(), a.getTotal()));



        return results;
    }
}