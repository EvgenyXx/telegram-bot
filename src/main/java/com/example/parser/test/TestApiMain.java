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

    public static void main(String[] args) {
        try {
            String url = "https://masters-league.com/wp-admin/admin-ajax.php";

            String searchName = "Милинка Владислав"; // 👈 ВОТ ТУТ ИГРОК

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            for (int i = 0; i < 3; i++) {
                String date = LocalDate.now().plusDays(i).toString();

                System.out.println("\n📅 CHECK DATE: " + date);

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

                List<TournamentDto> tournaments = mapper.readValue(
                        json,
                        new TypeReference<List<TournamentDto>>() {
                        }
                );

                for (TournamentDto t : tournaments) {

                    if (t.getPlayers() == null) continue;

                    for (String player : t.getPlayers()) {

                        if (player == null) continue;

                        // 💥 СРАВНЕНИЕ
                        if (normalize(player).equals(normalize(searchName))) {

                            System.out.println("\n🔥 НАЙДЕН ИГРОК: " + player);
                            System.out.println("===============================");

                            System.out.println("ID: " + t.getId());
                            System.out.println("TITLE: " + t.getTitle());
                            System.out.println("LEAGUE: " + t.getLeague());
                            System.out.println("HALL: " + t.getHall());

                            if (t.getDate() != null) {
                                String raw = t.getDate().getDate();

                                System.out.println("RAW DATE: " + raw);

                                if (raw != null && raw.length() >= 16) {
                                    String datePart = raw.substring(0, 10);
                                    String timePart = raw.substring(11, 16);

                                    System.out.println("📅 DATE: " + datePart);
                                    System.out.println("⏰ TIME: " + timePart);
                                }
                            }

                            System.out.println("PLAYERS: " + t.getPlayers());
                            System.out.println("LINK: " + t.getLink());

                            break; // нашли — выходим из игроков
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String normalize(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}