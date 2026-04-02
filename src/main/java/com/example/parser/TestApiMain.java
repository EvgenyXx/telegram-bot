package com.example.parser;

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

        String searchName = "Павлов Евгений".toLowerCase();

        String url = "https://masters-league.com/wp-admin/admin-ajax.php";

        // 👉 завтра
        String date = LocalDate.now().plusDays(1).toString();

        // 1. Запрос к API
        Connection.Response res = Jsoup.connect(url)
                .method(Connection.Method.POST)
                .header("User-Agent", "Mozilla/5.0")
                .data("action", "tourslist")
                .data("date", date)
                .data("country", "RUS")
                .ignoreContentType(true)
                .timeout(10000)
                .execute();

        String json = res.body();

        // 2. Парсинг JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<TournamentDto> tournaments =
                mapper.readValue(json, new TypeReference<List<TournamentDto>>() {});

        boolean found = false;

        // 3. Поиск игрока
        for (TournamentDto t : tournaments) {

            if (t.getPlayers() == null) continue;

            for (String player : t.getPlayers()) {

                if (player == null) continue;

                if (player.toLowerCase().contains(searchName)) {

                    found = true;

                    System.out.println("🔥 НАЙДЕН ТУРНИР!");
                    System.out.println("Дата: " + date);
                    System.out.println("ID: " + t.getId());
                    System.out.println("Лига: " + t.getLeague());
                    System.out.println("Зал: " + t.getHall());
                    System.out.println("Игроки: " + t.getPlayers());
                    System.out.println("=================================");
                }
            }
        }

        if (!found) {
            System.out.println("❌ Павлов Евгений не найден на " + date);
        }
    }
}