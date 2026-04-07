package com.example.parser.test;

import com.example.parser.domain.dto.TournamentDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.time.LocalDate;
import java.util.List;

public class TestApiMain {

    public static void main(String[] args) throws Exception {

        String url = "https://masters-league.com/wp-admin/admin-ajax.php";

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        for (int i = 0; i < 3; i++) {

            String date = LocalDate.now().plusDays(i).toString();

            System.out.println("\n==============================");
            System.out.println("DATE: " + date);
            System.out.println("==============================");

            Connection.Response res = Jsoup.connect(url)
                    .method(Connection.Method.POST)
                    .header("User-Agent", "Mozilla/5.0")
                    .data("action", "tourslist")
                    .data("date", date)
                    .data("country", "RUS")
                    .ignoreContentType(true)
                    .timeout(10000)
                    .execute();

            // 🔥 статус ответа
            System.out.println("STATUS: " + res.statusCode());

            // 🔥 сырой JSON
            String json = res.body();

            System.out.println("\n==== RAW JSON ====");
            System.out.println(json);
            System.out.println("==== END JSON ====\n");

            // 🔥 пробуем парсить
            try {
                List<TournamentDto> tournaments = mapper.readValue(
                        json,
                        new TypeReference<List<TournamentDto>>() {}
                );

                System.out.println("PARSED SIZE: " + tournaments.size());

                for (TournamentDto t : tournaments) {
                    System.out.println("\n=== TOURNAMENT ===");
                    System.out.println("ID: " + t.getId());
                    System.out.println("TITLE: " + t.getTitle());
                    System.out.println("DATE: " + t.getDate());
                    System.out.println("PLAYERS: " + t.getPlayers());
                }

            } catch (Exception e) {
                System.out.println("❌ JSON НЕ РАСПАРСИЛСЯ");
                e.printStackTrace();
            }
        }
    }
}