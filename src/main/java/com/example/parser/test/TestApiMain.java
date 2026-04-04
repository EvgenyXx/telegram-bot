package com.example.parser.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class TestApiMain {

    public static void main(String[] args) throws Exception {
        String url = "https://masters-league.com/tours/liga-d-7170/";

        Document doc = Jsoup.connect(url).get();

        System.out.println("TITLE: " + doc.title());

        boolean started = isTournamentStarted(doc);

        System.out.println("STARTED = " + started);
    }

    public static boolean isTournamentStarted(Document doc) {
        var matches = doc.select(".ml_tour_game_list_row");

        System.out.println("TOTAL MATCHES: " + matches.size());

        if (matches.isEmpty()) {
            return false;
        }

        // 👉 берём первый реальный матч
        Element firstMatch = matches.stream()
                .filter(m -> !m.text().contains("Статус"))
                .findFirst()
                .orElse(null);

        if (firstMatch == null) {
            System.out.println("FIRST MATCH NOT FOUND");
            return false;
        }

        // 🔥 ВОТ ОН — ПЕРВЫЙ МАТЧ
        System.out.println("\n==== FIRST MATCH ONLY ====");
        System.out.println(firstMatch.outerHtml());
        System.out.println("=================================\n");

        // 👉 статус ТОЛЬКО этого матча
        Element status = firstMatch.selectFirst(".ml_tour_game_status");

        if (status == null) {
            System.out.println("STATUS NOT FOUND IN FIRST MATCH");
            return false;
        }

        String classes = status.className();

        System.out.println("FIRST MATCH STATUS CLASS: " + classes);

        boolean isStarted = classes.contains("goes");

        System.out.println("FIRST MATCH STARTED: " + isStarted);

        return isStarted;
    }
}