package com.example.parser.formatter;


import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LiveMatchFormatter {

    public String formatLine(String name, int score, String sets, boolean isFirst) {
        String shortName = shortenName(name);

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
        for (Integer v : values) {
            setsStr.append(String.format("%3d ", v)); // 🔥 ВОТ ТУТ
        }

        return String.format("%-16s %2d   %s",  // 🔥 И ТУТ
                shortName,
                score,
                setsStr.toString().trim()
        );
    }

    private String shortenName(String fullName) {
        String[] parts = fullName.split(" ");
        if (parts.length < 2) return fullName;

        return parts[0] + " " + parts[1].charAt(0) + ".";
    }
}
