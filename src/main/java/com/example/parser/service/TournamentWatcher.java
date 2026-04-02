package com.example.parser.service;

import com.example.parser.bot.ParserBot;
import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.domain.entity.PlayerSubscription;
import com.example.parser.repository.PlayerSubscriptionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TournamentWatcher {

    private final PlayerSubscriptionRepository repo;
    private final MessageService messageService;
    private final ParserBot bot;

    private final Set<String> sentCache = new HashSet<>();

    @Scheduled(fixedRate = 10000) // каждые 5 минут
    public void check() throws Exception {

        System.out.println("🔥 CHECK ЗАПУЩЕН: " + LocalDate.now());

        List<PlayerSubscription> subs = repo.findAll();

        for (int i = 0; i < 3; i++) {

            String date = LocalDate.now().plusDays(i).toString();

            String url = "https://masters-league.com/wp-admin/admin-ajax.php";

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

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            List<TournamentDto> tournaments =
                    mapper.readValue(json, new TypeReference<List<TournamentDto>>() {});

            for (TournamentDto t : tournaments) {

                if (t.getPlayers() == null) continue;

                for (String player : t.getPlayers()) {

                    if (player == null) continue;

                    for (PlayerSubscription sub : subs) {

                        if (player.toLowerCase().contains(sub.getPlayerName().toLowerCase())) {

                            String key = sub.getTelegramId() + "_" + t.getId();

                            if (sentCache.contains(key)) continue;

                            sentCache.add(key);

                            messageService.send(bot, sub.getTelegramId(),
                                    "🏓 " + player + "\n" +
                                            "📅 " + date + "\n" +
                                            "🏆 Турнир найден!\n" +
                                            "Лига: " + t.getLeague() + "\n" +
                                            "Зал: " + t.getHall()
                            );
                        }
                    }
                }
            }
        }
    }
}