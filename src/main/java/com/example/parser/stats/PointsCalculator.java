package com.example.parser.stats;

import com.example.parser.domain.model.Match;

public interface PointsCalculator {
    int calculatePoints(Match match);
}