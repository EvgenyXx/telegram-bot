package com.example.parser.notification;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.domain.entity.Tournament;
import com.example.parser.integration.DocumentLoader;
import com.example.parser.notification.formatter.TournamentCancelledMessageBuilder;
import com.example.parser.tournament.ResultService;
import com.example.parser.tournament.parser.TournamentParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentFinishScheduler {

    private final PlayerNotificationRepository repo;
    private final DocumentLoader documentLoader;
    private final TournamentParser tournamentParser;
    private final ResultService resultService;
    private final TournamentProcessService processService;

    // 🔥 добавили
    private final NotificationService notificationService;
    private final TournamentCancelledMessageBuilder cancelledMessageBuilder;

    @Scheduled(fixedRate = 420000, initialDelay = 30000)
    public void checkFinished() {
        List<PlayerNotification> list = repo.findNotFinishedFull();

        Map<String, List<PlayerNotification>> grouped = list.stream()
                .filter(p -> p.getTournament() != null)
                .collect(Collectors.groupingBy(p -> p.getTournament().getLink()));

        grouped.forEach(this::processSafe);
    }

    private void processSafe(String link, List<PlayerNotification> notifications) {
        try {
            if (link == null) return;

            Tournament tournament = notifications.get(0).getTournament();
            if (tournament == null) return;

            if (tournament.isFinished()) return;
            if (tournament.getDate() == null || tournament.getTime() == null) return;

            Document document = documentLoader.load(link);

            // ❌ ОТМЕНА (первая проверка)
            if (tournamentParser.isCancelled(document)) {

                // защита от повторной отправки
                if (tournament.isCancelled()) return;

                tournament.setCancelled(true);

                sendCancelledNotifications(notifications);

                repo.saveAll(notifications);

                log.info("❌ tournament cancelled: id={}, users={}",
                        tournament.getExternalId(),
                        notifications.size());

                return;
            }

            // 🏁 завершение
            if (!tournamentParser.isFinished(document)) return;

            ResultService.ParsedResult parsed = resultService.calculateAll(document);

            processService.processTournament(notifications, parsed);

            tournament.setFinished(true);

            repo.saveAll(notifications);

            log.info("🏁 tournament finished: id={}, users={}",
                    tournament.getExternalId(),
                    notifications.size());

        } catch (Exception e) {
            log.error("failed to process tournament: link={}", link, e);
        }
    }

    // ❌ отправка отмены
    private void sendCancelledNotifications(List<PlayerNotification> notifications) {
        List<Long> ids = notifications.stream()
                .map(PlayerNotification::getId)
                .toList();

        Map<Long, Long> telegramMap = repo.findTelegramIdsByNotificationIds(ids)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        for (PlayerNotification pn : notifications) {
            Long telegramId = telegramMap.get(pn.getId());
            if (telegramId == null) continue;

            try {
                notificationService.send(
                        telegramId,
                        cancelledMessageBuilder.build(pn)
                );
            } catch (Exception e) {
                log.error("cancel send failed: {}", telegramId, e);
            }
        }
    }
}