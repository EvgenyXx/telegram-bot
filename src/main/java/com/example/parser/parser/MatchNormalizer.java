package com.example.parser.parser;

import com.example.parser.domain.model.Match;
import org.springframework.stereotype.Component;

@Component
public class MatchNormalizer {

    public Match normalize(Match match, String targetPlayer) {

        String p2 = normalize(match.getPlayer2());
        String target = normalize(targetPlayer);

        if (p2.equals(target)) {
            String tempPlayer = match.getPlayer1();
            match.setPlayer1(match.getPlayer2());
            match.setPlayer2(tempPlayer);

            int tempScore = match.getScore1();
            match.setScore1(match.getScore2());
            match.setScore2(tempScore);
        }

        return match;
    }

    private String normalize(String name) {
        if (name == null) return "";
        return name.toLowerCase().trim().replaceAll("\\s+", " ");
    }
}