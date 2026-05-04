package ru.pulsecore.app.core.stats;

import ru.pulsecore.app.core.model.Match;

public interface PointsCalculator {
    int calculatePoints(Match match);
}