package com.example.parser.formatter;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class LiveMatchFormatter {

    public String formatLine(String name, int score, String sets, boolean isFirst) {

        String shortName = shortenName(name); // 👈 используем

        List<Integer> values = new ArrayList<>();

        if (sets != null && !sets.isEmpty()) {
            String[] setsArr = sets.replace("(", "").replace(")", "").split(" ");

            for (String set : setsArr) {
                String[] parts = set.split(":");
                if (parts.length != 2) continue;

                int value = Integer.parseInt(isFirst ? parts[0] : parts[1]);
                values.add(value);
            }
        }

        StringBuilder setsStr = new StringBuilder();

        int maxSets = 5; // фикс колонок

        for (int i = 0; i < maxSets; i++) {
            if (i < values.size()) {
                setsStr.append(String.format("%3d ", values.get(i)));
            } else {
                setsStr.append("  - ");
            }
        }

        return String.format("%-16s %2d   %s", // 👈 КЛЮЧЕВАЯ СТРОКА
                shortName,
                score,
                setsStr.toString().trim()
        );
    }

    // 🔥 ВСТАВЛЯЕШЬ СЮДА
    private String shortenName(String fullName) {
        String[] parts = fullName.split(" ");
        if (parts.length < 2) return trimToLength(fullName, 16);

        String shortName = parts[0] + " " + parts[1].charAt(0) + ".";
        return trimToLength(shortName, 16);
    }

    // 🔥 И ЭТО ТОЖЕ СЮДА
    private String trimToLength(String text, int max) {
        if (text.length() <= max) return text;
        return text.substring(0, max - 1) + "…";
    }
}