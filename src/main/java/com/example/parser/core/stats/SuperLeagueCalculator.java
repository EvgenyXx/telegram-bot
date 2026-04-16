package com.example.parser.core.stats;

import com.example.parser.core.model.Match;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SuperLeagueCalculator implements PointsCalculator{

    @Override
    public int calculatePoints(Match match) {

        int a = match.getScore1();

        if (a == 4) return 2000;
        if (a == 3) return 1300;
        if (a == 2) return 1000;
        if (a == 1) return 700;
        return 400;
    }
}
