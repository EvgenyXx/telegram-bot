package com.example.parser;

import com.example.parser.service.LeagueDetector;
import org.springframework.stereotype.Service;



@Service
public class ResultService {

    private final MatchParser parser;
    private final MatchNormalizer normalizer;
    private final PlacementCalculator placementCalculator;
    private final BonusCalculator bonusCalculator;
    private final LeagueDetector leagueDetector;
    private final PointsCalculatorFactory factory;

    public ResultService(
            MatchParser parser,
            MatchNormalizer normalizer,
            PlacementCalculator placementCalculator,
            BonusCalculator bonusCalculator,
            LeagueDetector leagueDetector,
            PointsCalculatorFactory factory
    ) {
        this.parser = parser;
        this.normalizer = normalizer;
        this.placementCalculator = placementCalculator;
        this.bonusCalculator = bonusCalculator;
        this.leagueDetector = leagueDetector;
        this.factory = factory;
    }

    public Result calculate(String url, String targetPlayer) throws Exception {

        // 🔥 определяем лигу
        LeagueType league = leagueDetector.detectLeague(url);

        // 🔥 берем нужный калькулятор
        PointsCalculator pointsCalculator = factory.getCalculator(league);

        var matches = parser.parseMatches(url);

        int total = 0;
        int place = 0;

        for (Match m : matches) {

            m = normalizer.normalize(m, targetPlayer);

            if (!m.getPlayer1().equals(targetPlayer)) continue;

            int points = pointsCalculator.calculatePoints(m);
            total += points;

            int p = placementCalculator.calculatePlace(m);
            if (p != 0) place = p;
        }

        int bonus = bonusCalculator.getBonus(place);
        total += bonus;

        return new Result(place, bonus, total);
    }
}