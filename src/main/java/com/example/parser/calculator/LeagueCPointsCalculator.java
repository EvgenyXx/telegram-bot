package com.example.parser.calculator;

import com.example.parser.domain.model.Match;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeagueCPointsCalculator implements PointsCalculator{
    @Override
    public int calculatePoints(Match match) {
        int a = match.getScore1();

        if (a == 4) return 900;
        if (a == 3) return 500;
        if (a == 2) return 400;
        if (a == 1) return 300;
        return 200;
    }
}
