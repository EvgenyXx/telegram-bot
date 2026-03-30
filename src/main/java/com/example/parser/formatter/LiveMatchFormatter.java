package com.example.parser.formatter;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class LiveMatchFormatter {

    public String formatLine(String name, int score, String sets, boolean isFirst) {
        String shortName = shortenName(name);

        List<String> values = new ArrayList<>();

        if (sets != null && !sets.isEmpty()) {
            String[] setsArr = sets.replace("(", "").replace(")", "").split(" ");

            for (String set : setsArr) {
                String[] parts = set.split(":");
                if (parts.length != 2) continue;

                values.add(isFirst ? parts[0] : parts[1]);
            }
        }

        StringBuilder setsStr = new StringBuilder();

        for (int i = 0; i < values.size(); i++) {
            String v = values.get(i);

            if (i == values.size() - 1) {
                setsStr.append(String.format("%3s", v)).append("•");
            } else {
                setsStr.append(String.format("%3s", v));
            }
        }

        // до 7 сетов
        while (values.size() < 7) {
            setsStr.append(String.format("%3s", "-"));
            values.add("-");
        }

        return String.format("%-9s %1d%s",
                shortName,
                score,
                setsStr.toString()
        );
    }

    // 🔥 ВСТАВЛЯЕШЬ СЮДА
    private String shortenName(String fullName) {
        String[] parts = fullName.split(" ");
        if (parts.length < 2) return trimToLength(fullName, 9);

        String shortName = parts[0] + " " + parts[1].charAt(0) + ".";
        return trimToLength(shortName, 9);
    }

    // 🔥 И ЭТО ТОЖЕ СЮДА
    private String trimToLength(String text, int max) {
        if (text.length() <= max) return text;
        return text.substring(0, max - 1) + "…";
    }
}