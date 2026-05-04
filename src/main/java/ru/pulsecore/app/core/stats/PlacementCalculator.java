package ru.pulsecore.app.core.stats;

import ru.pulsecore.app.core.model.Match;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PlacementCalculator {

    public int calculatePlace(Match match) {

        String stage = match.getStage();
        int a = match.getScore1();
        int b = match.getScore2();



        if (stage.equals("Финал")) {

            return (a == 4) ? 1 : 2;
        }

        if (stage.equals("За 3-е место")) {

            return (a == 4) ? 3 : 4;
        }


        return 0;
    }
}