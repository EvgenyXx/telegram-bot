package com.example.parser.stats;

import org.springframework.stereotype.Component;

@Component
public class BonusCalculator {

    public int getBonus(int place) {

        System.out.println("CALCULATING BONUS FOR PLACE: " + place);

        if (place == 1) return 1000;
        if (place == 2) return 600;
        if (place == 3) return 400;

        return 0;
    }
}