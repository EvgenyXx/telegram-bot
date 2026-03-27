package com.example.parser.calculator;

import com.example.parser.model.Match;
import org.springframework.stereotype.Component;

@Component
public class PlacementCalculator {

    public int calculatePlace(Match match) {

        String stage = match.getStage();
        int a = match.getScore1();

        System.out.println("CALCULATING PLACE...");
        System.out.println("STAGE: " + stage);
        System.out.println("SCORE1: " + a);

        if (stage.equals("Финал")) {
            int result = (a == 4) ? 1 : 2;
            System.out.println("FINAL MATCH PLACE: " + result);
            return result;
        }

        if (stage.equals("За 3-е место")) {
            int result = (a == 4) ? 3 : 4;
            System.out.println("3RD PLACE MATCH RESULT: " + result);
            return result;
        }

        System.out.println("NO PLACE (group stage)");
        return 0;
    }
}