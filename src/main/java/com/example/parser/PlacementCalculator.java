package com.example.parser;

import org.springframework.stereotype.Component;

@Component
public class PlacementCalculator {

    public int calculatePlace(Match match) {

        String stage = match.getStage();
        int a = match.getScore1();

        if (stage.equals("Финал")) {
            return (a == 4) ? 1 : 2;
        }

        if (stage.equals("За 3-е место")) {
            return (a == 4) ? 3 : 4;
        }

        return 0; // для групп и полуфиналов
    }
}