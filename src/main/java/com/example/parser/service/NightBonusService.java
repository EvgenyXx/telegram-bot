package com.example.parser.service;

import com.example.parser.config.HtmlSelectors;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class NightBonusService {



    private static final LocalTime NIGHT_BORDER = LocalTime.of(6, 0);

    public boolean isNight(Document doc) {
        String time = extractTime(doc);
        LocalTime startTime = parseTime(time);
        return startTime.isBefore(NIGHT_BORDER);
    }

    public double getBonus(String league) {
        return switch (league) {
            case "A" -> 1000;
            case "B" -> 750;
            case "C" -> 500;
            case "D" -> 200;
            default -> 0;
        };
    }

    public double calculateBonus(Document doc, String league) {
        return isNight(doc) ? getBonus(league) : 0;
    }

    // ----------------- private helpers -----------------

    private String extractTime(Document doc) {
        return doc.select(HtmlSelectors.TIME_ROW_SELECTOR)
                .stream()
                .filter(el -> el.text().contains(HtmlSelectors.TIME_LABEL))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Не найдено время турнира"))
                .select(HtmlSelectors.TD_SELECTOR)
                .text();
    }

    private LocalTime parseTime(String rawTime) {
        String normalized = rawTime.length() > 5
                ? rawTime.substring(0, 5)
                : rawTime;

        return LocalTime.parse(normalized);
    }
}