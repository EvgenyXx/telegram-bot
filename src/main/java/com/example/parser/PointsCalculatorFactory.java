package com.example.parser;

import com.example.parser.service.LeagueAPointsCalculator;
import org.springframework.stereotype.Component;

@Component
public class PointsCalculatorFactory {

    private final LeagueAPointsCalculator leagueA;
    private final LeagueBPointsCalculator leagueB;

    public PointsCalculatorFactory(
            LeagueAPointsCalculator leagueA,
            LeagueBPointsCalculator leagueB
    ) {
        this.leagueA = leagueA;
        this.leagueB = leagueB;
    }

    public PointsCalculator getCalculator(LeagueType league) {

        return switch (league) {
            case A -> leagueA;
            case B -> leagueB;
            default -> throw new RuntimeException("Нет калькулятора для лиги: " + league);
        };
    }
}