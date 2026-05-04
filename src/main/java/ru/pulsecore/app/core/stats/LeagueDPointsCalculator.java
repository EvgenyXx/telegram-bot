package ru.pulsecore.app.core.stats;

import ru.pulsecore.app.core.model.Match;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeagueDPointsCalculator implements PointsCalculator{
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
