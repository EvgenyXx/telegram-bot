package com.example.parser;

import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.service.UpcomingTournamentService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.time.LocalDate;
import java.util.List;

public class TestApiMain {

    public static void main(String[] args) throws Exception {

        UpcomingTournamentService service = new UpcomingTournamentService();

        List<TournamentDto> tournaments =
                service.findPlayerTournaments("Милинка владислав ");

        System.out.println("\nRESULT SIZE: " + tournaments.size());

        for (TournamentDto t : tournaments) {
            System.out.println("=================================");
            System.out.println("🆔 ID: " + t.getId());
            System.out.println("🏆 Лига: " + t.getLeague());
            System.out.println("📍 Зал: " + t.getHall());
        }

//        String searchName = "Милинка владислав ";
//        String url = "https://masters-league.com/wp-admin/admin-ajax.php";
//
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//        boolean foundAny = false;
//
//        for (int i = 0; i < 7; i++) {
//            String date = LocalDate.now().plusDays(i).toString();
//
//            Connection.Response res = Jsoup.connect(url)
//                    .method(Connection.Method.POST)
//                    .header("User-Agent", "Mozilla/5.0")
//                    .data("action", "tourslist")
//                    .data("date", date)
//                    .data("country", "RUS")
//                    .ignoreContentType(true)
//                    .timeout(10000)
//                    .execute();
//
//            String json = res.body();
//
//            List<TournamentDto> tournaments = mapper.readValue(
//                    json,
//                    new TypeReference<List<TournamentDto>>() {}
//            );
//
//            for (TournamentDto t : tournaments) {
//                if (t.getPlayers() == null) continue;
//
//                for (String player : t.getPlayers()) {
//                    if (player == null) continue;
//
//                    if (isSamePlayer(searchName, player)) {
//                        foundAny = true;
//
//                        System.out.println("\n🔥 НАЙДЕН ТУРНИР");
//                        System.out.println("📅 Дата: " + date);
//                        System.out.println("🆔 ID: " + t.getId());
//                        System.out.println("🏆 Лига: " + t.getLeague());
//                        System.out.println("📍 Зал: " + t.getHall());
//                        System.out.println("👤 Игрок: " + player);
//                        System.out.println("=================================");
//                    }
//                }
//            }
//        }
//
//        if (!foundAny) {
//            System.out.println("\n❌ Игрок не найден ни в одном дне");
//        }
//    }
//
//    // =========================
//    // НОРМАЛИЗАЦИЯ
//    // =========================
//    private static String normalize(String name) {
//        if (name == null) return "";
//
//        return name
//                .toLowerCase()
//                .replace("\u00A0", " ")
//                .replaceAll("\\s+", " ")
//                .trim();
//    }
//
//    // =========================
//    // СРАВНЕНИЕ (ТОЛЬКО ЧЕТКОЕ)
//    // =========================
//    private static boolean isSamePlayer(String n1, String n2) {
//        String p1 = normalize(n1);
//        String p2 = normalize(n2);
//
//        return p1.equals(p2);
//    }
    }
}