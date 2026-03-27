package com.example.parser.calculator;

import com.example.parser.model.Match;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class LeagueBPointsCalculator implements PointsCalculator {

    @Override
    public int calculatePoints(Match match) {

        int a = match.getScore1();

        System.out.println("CALCULATING POINTS (League B), SCORE1: " + a);

        if (a == 4) return 1200;
        if (a == 3) return 650;
        if (a == 2) return 500;
        if (a == 1) return 350;

        return 200;
    }
}