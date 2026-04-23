package com.example.parser.modules.tournament.parser;

import com.example.parser.modules.tournament.domain.model.Score;
import org.springframework.stereotype.Component;

@Component
public class ScoreParser {

    public Score parseScore(String scoreText) {
        if (scoreText == null) {
            return null;
        }

        scoreText = scoreText.trim();

        if (!scoreText.contains(":")) {
            return null;
        }

        int bracketIndex = scoreText.indexOf("(");
        if (bracketIndex != -1) {
            scoreText = scoreText.substring(0, bracketIndex).trim();
        }

        String[] parts = scoreText.split(":");

        if (parts.length < 2) {
            return null;
        }

        try {
            int s1 = Integer.parseInt(parts[0].trim());
            int s2 = Integer.parseInt(parts[1].trim());

            return new Score(s1, s2);
        } catch (Exception e) {
            return null;
        }
    }

    public String extractSets(String fullScore) {
        if (fullScore == null) {
            return "";
        }

        int bracketIndex = fullScore.indexOf("(");

        if (bracketIndex != -1) {
            return fullScore.substring(bracketIndex).trim();
        }

        return "";
    }
}