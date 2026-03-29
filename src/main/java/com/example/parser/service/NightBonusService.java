package com.example.parser.service;



import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class NightBonusService {

    public boolean isNight(Document doc) {
        String time = doc.select("table.info_table tr")
                .stream()
                .filter(el -> el.text().contains("Время"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Не найдено время турнира"))
                .select("td")
                .text();

        time = time.length() > 5 ? time.substring(0, 5) : time;

        LocalTime startTime = LocalTime.parse(time);

        return startTime.isBefore(LocalTime.of(6, 0));
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

    public double applyBonus(Document doc, String league, double amount) {
        if (isNight(doc)) {
            return amount + getBonus(league);
        }
        return amount;
    }

    public double calculateBonus(Document doc, String league) {
        if (isNight(doc)) {
            return getBonus(league);
        }
        return 0;
    }
}