package com.example.parser.service;

import com.example.parser.domain.dto.TournamentDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UpcomingTournamentService {

    public List<TournamentDto> findPlayerTournaments(String searchName) {

        System.out.println("🚨 START CHECK: [" + searchName + "]");

        List<TournamentDto> result = new ArrayList<>();

        try {
            String url = "https://masters-league.com/wp-admin/admin-ajax.php";

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            for (int i = 0; i < 3; i++) {
                String date = LocalDate.now().plusDays(i).toString();

                System.out.println("📅 DATE: " + date);

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
                        new TypeReference<List<TournamentDto>>() {}
                );

                for (TournamentDto t : tournaments) {
                    if (t.getPlayers() == null) continue;

                    for (String player : t.getPlayers()) {
                        if (player == null) continue;

                        if (isSamePlayer(searchName, player)) {
                            System.out.println("🔥 FOUND: " + player);

                            result.add(t); // 💥 ВОТ ЭТО ГЛАВНОЕ
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private boolean isSamePlayer(String n1, String n2) {
        return normalize(n1).equals(normalize(n2));
    }

    private String normalize(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}