package com.example.parser.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class TestApiMain {

    public static void main(String[] args) {

        // 🔥 сюда подставляешь любую ссылку турнира
        String link = "https://masters-league.com/tours/liga-c-1015/";

        if (isTournamentCancelled(link)) {
            System.out.println("❌ ТУРНИР ОТМЕНЕН");
        } else {
            System.out.println("✅ турнир НЕ отменен");
        }
    }

    // 🔥 МЕТОД ПРОВЕРКИ
    public static boolean isTournamentCancelled(String link) {
        try {
            Document doc = Jsoup.connect(link)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Elements statuses = doc.select(".ml_tour_game_status");

            if (statuses.isEmpty()) {
                System.out.println("⚠️ статусы не найдены");
                return false;
            }

            // 🔥 УБИРАЕМ ЗАГОЛОВКИ И МУСОР
            var realStatuses = statuses.stream()
                    .filter(el -> !el.text().equalsIgnoreCase("Статус"))
                    .toList();

            // дебаг
            realStatuses.forEach(el ->
                    System.out.println("REAL STATUS: " + el.text() + " | " + el.className())
            );

            if (realStatuses.isEmpty()) return false;

            // 🔥 ТЕПЕРЬ ПРОВЕРКА
            return realStatuses.stream().allMatch(el ->
                    el.className().contains("canceled") ||
                            el.text().toLowerCase().contains("отменен")
            );

        } catch (Exception e) {
            System.out.println("❌ ошибка загрузки: " + link);
            e.printStackTrace();
            return false;
        }

    }
}