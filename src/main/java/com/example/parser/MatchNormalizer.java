package com.example.parser;

import org.springframework.stereotype.Component;

@Component
public class MatchNormalizer {

    public Match normalize(Match match, String targetPlayer) {

        // если наш игрок справа — переворачиваем
        if (match.getPlayer2().equals(targetPlayer)) {

            String tempPlayer = match.getPlayer1();
            match.setPlayer1(match.getPlayer2());
            match.setPlayer2(tempPlayer);

            int tempScore = match.getScore1();
            match.setScore1(match.getScore2());
            match.setScore2(tempScore);
        }

        return match;
    }
}