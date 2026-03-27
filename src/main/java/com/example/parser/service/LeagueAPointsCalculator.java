package com.example.parser.service;

import com.example.parser.Match;
import com.example.parser.PointsCalculator;
import org.springframework.stereotype.Service;

@Service
public class LeagueAPointsCalculator implements PointsCalculator {

    @Override
    public int calculatePoints(Match match) {

        int a = match.getScore1();

        if (a == 4) return 1700;
        if (a == 3) return 1000;
        if (a == 2) return 800;
        if (a == 1) return 600;
        return 400;
    }
}