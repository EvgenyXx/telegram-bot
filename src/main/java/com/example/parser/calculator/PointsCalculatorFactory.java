package com.example.parser.calculator;

import com.example.parser.model.LeagueType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointsCalculatorFactory {

    private final LeagueAPointsCalculator leagueA;
    private final LeagueBPointsCalculator leagueB;
    private final LeagueCPointsCalculator leagueC;

//    public PointsCalculatorFactory(
//            LeagueAPointsCalculator leagueA,
//            LeagueBPointsCalculator leagueB
//    ) {
//        this.leagueA = leagueA;
//        this.leagueB = leagueB;
//    }

    public PointsCalculator getCalculator(LeagueType league) {

        return switch (league) {
            case A -> leagueA;
            case B -> leagueB;
            case C -> leagueC;
            default -> throw new RuntimeException("Нет калькулятора для лиги: " + league);
        };
    }
}