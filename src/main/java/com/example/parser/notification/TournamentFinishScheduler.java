package com.example.parser.notification;


import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.integration.DocumentLoader;
import com.example.parser.notification.formatter.TournamentMessageFormatter;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import com.example.parser.tournament.ResultService;
import com.example.parser.tournament.TournamentResultService;
import com.example.parser.tournament.parser.TournamentParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentFinishScheduler {

    private final PlayerNotificationRepository repo;
    private final TournamentResultService tournamentResultService;
    private final NotificationService notificationService;
    private final PlayerService playerService;
    private final ResultService resultService;
    private final DocumentLoader documentLoader;
    private final TournamentParser tournamentParser;
    private final TournamentMessageFormatter messageFormatter;

    @Scheduled(fixedRate = 420000) // 7 минут
    public void checkFinished() {

        List<PlayerNotification> list = repo.findByFinishedFalse();

        // 🔥 группировка по турниру (link)
        Map<String, List<PlayerNotification>> grouped =
                list.stream()
                        .collect(Collectors.groupingBy(PlayerNotification::getLink));

        for (var entry : grouped.entrySet()) {

            String link = entry.getKey();
            List<PlayerNotification> notifications = entry.getValue();

            try {

                if (link == null) continue;

                // берем любой элемент как "представителя" турнира
                PlayerNotification sample = notifications.get(0);

                // ✅ 1. НЕ будущее
                if (sample.getDate() != null &&
                        sample.getDate().isAfter(LocalDate.now())) {
                    continue;
                }

                log.info("⏱ {} | pnDate={} | now={} | started={} | finished={}",
                        sample.getTournamentId(),
                        sample.getDate(),
                        LocalDate.now(),
                        sample.getStarted(),
                        sample.getFinished());

                // 🔥 один раз грузим турнир
                Document document = documentLoader.load(link);

                if (!tournamentParser.isFinished(document)) {
                    continue;
                }

                ResultService.ParsedResult parsed =
                        resultService.calculateAll(document);

                // 🔁 теперь обрабатываем всех игроков этого турнира
                for (PlayerNotification pn : notifications) {

                    Player player = playerService.getByTelegramId(pn.getTelegramId());
                    if (player == null) continue;

                    boolean found = tournamentResultService.processResults(
                            parsed.getResults(),
                            player,
                            parsed.getTournamentId(),
                            parsed.getNightBonus(),
                            true
                    );

                    if (!found) continue;

                    boolean isRemoved = parsed.isHasRemovedPlayers();
                    String msg = messageFormatter.format(parsed.getResults(),parsed.isHasRemovedPlayers());
                    notificationService.send(pn.getTelegramId(), msg);

                    pn.setFinished(true);
                    repo.save(pn);
                }

                log.info("🏁 Tournament finished: {}", sample.getTournamentId());

            } catch (Exception e) {
                log.error("❌ ERROR {}", link, e);
            }
        }
    }

}