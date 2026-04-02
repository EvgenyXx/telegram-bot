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

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        boolean foundAny = false;

        // 👉 проверяем 7 дней вперёд
        for (int i = 0; i < 7; i++) {

            String date = LocalDate.now().plusDays(i).toString();

            System.out.println("🔍 Проверка даты: " + date);

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

            List<TournamentDto> tournaments =
                    mapper.readValue(json, new TypeReference<List<TournamentDto>>() {
                    });

            for (TournamentDto t : tournaments) {

                if (t.getPlayers() == null) continue;

                for (String player : t.getPlayers()) {

                    if (player == null) continue;

                    String normalized = player.toLowerCase();

                    // 🔥 УМНЫЙ МАТЧ ПО ФАМИЛИИ
                    String lastName = searchName.split(" ")[0];

                    if (normalized.contains(lastName)) {

                        foundAny = true;

                        System.out.println("🔥 НАЙДЕН ТУРНИР!");
                        System.out.println("Дата: " + date);
                        System.out.println("ID: " + t.getId());
                        System.out.println("Лига: " + t.getLeague());
                        System.out.println("Зал: " + t.getHall());
                        System.out.println("Игрок: " + player);
                        System.out.println("=================================");
                    }
                }
            }
        }

        if (!foundAny) {
            System.out.println("❌ Игрок не найден ни в одном дне");
        }
    }
}