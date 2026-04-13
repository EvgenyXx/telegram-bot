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
    private final NotificationService notificationService;
    private final TournamentCancelledMessageBuilder cancelledMessageBuilder;

    @Scheduled(fixedRate = 420000)
    public void checkFinished() {

        List<PlayerNotification> list = repo.findNotFinishedFull();

        log.info("🔄 FinishScheduler tick: totalNotifications={}", list.size());

        Map<String, List<PlayerNotification>> grouped = list.stream()
                .filter(p -> p.getTournament() != null)
                .collect(Collectors.groupingBy(p -> p.getTournament().getLink()));

        int processed = 0;
        int finished = 0;
        int cancelled = 0;

        for (Map.Entry<String, List<PlayerNotification>> entry : grouped.entrySet()) {
            Result r = processSafe(entry.getKey(), entry.getValue());
            if (r == null) continue;

            processed++;
            if (r.finished) finished++;
            if (r.cancelled) cancelled++;
        }

        log.info("✅ FinishScheduler done: processed={}, finished={}, cancelled={}",
                processed, finished, cancelled);
    }

    private Result processSafe(String link, List<PlayerNotification> notifications) {

        if (!isValid(link, notifications)) return null;

        try {
            Tournament tournament = notifications.get(0).getTournament();

            if (tournament.isProcessed()) return null;

            Document document = documentLoader.load(link);

            // ❌ CANCELLED
            if (tournamentParser.isCancelled(document)) {

                if (tournament.isCancelled()) return null;

                tournament.setCancelled(true);
                tournament.setProcessed(true);

                sendCancelledNotifications(notifications);
                repo.saveAll(notifications);

                log.info("❌ tournament cancelled: id={}, users={}",
                        tournament.getExternalId(),
                        notifications.size());

                return new Result(false, true);
            }

            // 🏁 FINISHED
            if (!tournamentParser.isFinished(document)) return null;

            ResultService.ParsedResult parsed = resultService.calculateAll(document);

            processService.processTournament(notifications, parsed);

            tournament.setFinished(true);
            tournament.setProcessed(true);

            repo.saveAll(notifications);

            log.info("🏁 tournament finished: id={}, users={}, results={}",
                    tournament.getExternalId(),
                    notifications.size(),
                    parsed.getResults().size());

            return new Result(true, false);

        } catch (Exception e) {
            log.error("❌ failed to process tournament: link={}", link, e);
            return null;
        }
    }

    // ================== VALIDATION ==================
    private boolean isValid(String link, List<PlayerNotification> notifications) {
        if (link == null || notifications == null || notifications.isEmpty()) return false;

        Tournament t = notifications.get(0).getTournament();
        return t != null && t.getDate() != null && t.getTime() != null;
    }

    // ================== CANCEL SEND ==================
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

        int success = 0;
        int failed = 0;

        for (PlayerNotification pn : notifications) {
            Long telegramId = telegramMap.get(pn.getId());
            if (telegramId == null) continue;

            try {
                notificationService.send(
                        telegramId,
                        cancelledMessageBuilder.build(pn)
                );
                success++;
            } catch (Exception e) {
                failed++;
                log.error("❌ cancel send failed: telegramId={}", telegramId, e);
            }
        }

        log.info("📩 cancel notifications: success={}, failed={}", success, failed);
    }

    // ================== RESULT ==================
    private record Result(boolean finished, boolean cancelled) {}
}