package com.example.parser.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class TestApiMain {

    public static void main(String[] args) {
        String link = "https://masters-league.com/tours/liga-d-7198/";

        boolean started = isTournamentStarted(link);

        if (started) {
            System.out.println("🚀 ТУРНИР ИДЁТ");
        } else {
            System.out.println("⛔ турнир НЕ начался");
        }
    }

    public static boolean isTournamentStarted(String link) {
        try {
            Document doc = Jsoup.connect(link)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            var matches = doc.select(".ml_tour_game_list_row");

            if (matches.isEmpty()) {
                System.out.println("❌ матчей нет");
                return false;
            }

            for (Element match : matches) {
                if (match.text().contains("Статус")) continue;

                Element status = match.selectFirst(".ml_tour_game_status");
                if (status == null) continue;

                String classes = status.className();
                String text = status.text();

                // 🔥 ДЕБАГ
                System.out.println("STATUS TEXT: " + text);
                System.out.println("STATUS CLASS: " + classes);

                // 🔥 ВАЖНО: проверяем ВСЕ матчи
                if (classes.contains("goes") || classes.contains("completed")) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            System.out.println("❌ ошибка загрузки: " + link);
            e.printStackTrace();
            return false;
        }
    }
}