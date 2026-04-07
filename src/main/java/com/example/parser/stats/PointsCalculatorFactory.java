package com.example.parser.stats;

import com.example.parser.domain.model.LeagueType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PointsCalculatorFactory {

    private final LeagueAPointsCalculator leagueA;
    private final LeagueBPointsCalculator leagueB;
    private final LeagueCPointsCalculator leagueC;
    private final MenSuperLeagueCalculator menSuperLeagueCalculator;



    public PointsCalculator getCalculator(LeagueType league) {

        return switch (league) {
            case A -> leagueA;
            case B -> leagueB;
            case C -> leagueC;
            case MEN_SUPER_LEAGUE -> menSuperLeagueCalculator;
            default -> throw new RuntimeException("Нет калькулятора для лиги: " + league);
        };
    }
}