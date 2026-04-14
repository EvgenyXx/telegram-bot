package com.example.parser.test;

import com.example.parser.domain.dto.TournamentDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.time.LocalDate;
import java.util.List;

public class TestApiMain {

    public static void main(String[] args) {

        try {
            String url = "https://masters-league.com/wp-admin/admin-ajax.php";
            String date = LocalDate.now().plusDays(1).toString();

            ObjectMapper mapper = new ObjectMapper();

            Connection.Response res = Jsoup.connect(url)
                    .method(Connection.Method.POST)
                    .header("User-Agent", "Mozilla/5.0")
                    .data("action", "tourslist")
                    .data("date", date)
                    .data("country", "RUS")
                    .ignoreContentType(true)
                    .timeout(10000)
                    .execute();

            List<TournamentDto> tournaments = mapper.readValue(
                    res.body(),
                    new TypeReference<>() {}
            );

            System.out.println("📋 Ростов — составы на завтра\n");

            for (TournamentDto t : tournaments) {

                Integer hallNumber = extractHallNumber(t.getHall());

                // 🔥 только 10 и 11 зал
                if (hallNumber == null || (hallNumber != 10 && hallNumber != 11)) {
                    continue;
                }

                if (t.getPlayers() == null || t.getPlayers().isEmpty()) {
                    continue;
                }

                String time = extractTime(t);
                String players = String.join(", ", t.getPlayers());

                System.out.println(
                        t.getLeague() + " | " + time + " — " + players
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String extractTime(TournamentDto t) {
        try {
            String full = t.getDate().getDate();
            return full.substring(11, 16);
        } catch (Exception e) {
            return "??:??";
        }
    }

    private static Integer extractHallNumber(String hall) {
        if (hall == null) return null;
        try {
            return Integer.parseInt(hall.replaceAll("\\D+", ""));
        } catch (Exception e) {
            return null;
        }
    }
}