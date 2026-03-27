package com.example.parser;

import org.springframework.stereotype.Component;

@Component
public class BonusCalculator {

    public int getBonus(int place) {
        if (place == 1) return 1000;
        if (place == 2) return 600;
        if (place == 3) return 400;
        return 0;
    }
}