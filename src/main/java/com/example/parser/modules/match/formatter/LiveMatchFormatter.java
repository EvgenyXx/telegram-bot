package com.example.parser.modules.match.formatter;

import com.example.parser.core.dto.LiveMatchData;
import com.example.parser.core.model.Match;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class LiveMatchFormatter {

    public String build(LiveMatchData data) {

        Match match = data.getMatch();

        StringBuilder sb = new StringBuilder();

        sb.append(" LIVE\n");
        sb.append("Стол ").append(match.getTable()).append("\n");
        sb.append("Лига ").append(match.getLeague()).append("\n");

        sb.append(formatLine(
                match.getPlayer1(),
                match.getScore1(),
                match.getSetsDetails(),
                true
        )).append("\n");

        sb.append(formatLine(
                match.getPlayer2(),
                match.getScore2(),
                match.getSetsDetails(),
                false
        )).append("\n");

        sb.append("Группа");

        return sb.toString();
    }

    public String formatLine(String name, int score, String sets, boolean isFirst) {

        String shortName = shortenName(name);

        List<String> values = new ArrayList<>();

        if (sets != null && !sets.isEmpty()) {
            String[] setsArr = sets.replace("(", "").replace(")", "").split(" ");

            for (String set : setsArr) {
                String[] parts = set.split(":");
                if (parts.length != 2) continue;

                String value = isFirst ? parts[0] : parts[1];
                values.add(value);
            }
        }

        StringBuilder setsStr = new StringBuilder();

        for (int i = 0; i < values.size(); i++) {
            String v = values.get(i);

            if (i == values.size() - 1) {
                setsStr.append(String.format("%4s", "[" + v + "]"));
            } else {
                setsStr.append(String.format("%4s", v));
            }
        }

        while (values.size() < 5) {
            setsStr.append(String.format("%4s", "-"));
            values.add("-");
        }

        return String.format("%-16s %2d %s", shortName, score, setsStr.toString());
    }

    private String shortenName(String fullName) {
        String[] parts = fullName.split(" ");
        if (parts.length < 2) return trimToLength(fullName, 16);

        String shortName = parts[0] + " " + parts[1].charAt(0) + ".";
        return trimToLength(shortName, 16);
    }

    private String trimToLength(String text, int max) {
        if (text.length() <= max) return text;
        return text.substring(0, max - 1) + "…";
    }
}