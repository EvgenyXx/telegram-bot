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

    @Scheduled(fixedRate = 420000, initialDelay = 30000)
    public void checkFinished() {

        log.warn("🔄 FINISH SCHEDULER START");

        List<PlayerNotification> list = repo.findNotFinishedFull();

        log.warn("📊 total notifications={}", list.size());

        Map<String, List<PlayerNotification>> grouped = list.stream()
                .filter(p -> p.getTournament() != null)
                .collect(Collectors.groupingBy(p -> p.getTournament().getLink()));

        log.warn("📦 grouped tournaments={}", grouped.size());

        grouped.forEach(this::processSafe);
    }

    private void processSafe(String link, List<PlayerNotification> notifications) {

        log.warn("➡️ PROCESS FINISH: link={}, users={}", link, notifications.size());

        try {
            if (!isValid(link, notifications)) {
                log.warn("⏭ SKIP: invalid data");
                return;
            }

            Tournament tournament = notifications.get(0).getTournament();

            log.warn("📌 tournamentId={}, processed={}, cancelled={}, finished={}",
                    tournament.getExternalId(),
                    tournament.isProcessed(),
                    tournament.isCancelled(),
                    tournament.isFinished()
            );

            if (shouldSkipTournament(tournament)) {
                log.warn("⏭ SKIP: already processed");
                return;
            }

            Document document = documentLoader.load(link);

            if (handleCancelled(tournament, notifications, document)) {
                log.warn("⏭ STOP: cancelled handled");
                return;
            }

            handleFinished(tournament, notifications, document);

        } catch (Exception e) {
            log.error("❌ FAILED processing tournament: link={}", link, e);
        }
    }

    // 🔍 Проверяем валидность
    private boolean isValid(String link, List<PlayerNotification> notifications) {

        if (link == null) {
            log.warn("❌ INVALID: link is null");
            return false;
        }

        Tournament tournament = notifications.get(0).getTournament();

        if (tournament == null) {
            log.warn("❌ INVALID: tournament is null");
            return false;
        }

        if (tournament.getDate() == null || tournament.getTime() == null) {
            log.warn("❌ INVALID: no date/time");
            return false;
        }

        return true;
    }

    // ⛔ пропуск
    private boolean shouldSkipTournament(Tournament tournament) {
        return tournament.isProcessed();
    }

    // ❌ отмена
    private boolean handleCancelled(Tournament tournament,
                                    List<PlayerNotification> notifications,
                                    Document document) {

        if (!tournamentParser.isCancelled(document)) return false;

        log.warn("⚠️ DETECTED CANCELLED: tournament={}", tournament.getExternalId());

        if (tournament.isCancelled()) {
            log.warn("⏭ SKIP: already cancelled");
            return true;
        }

        tournament.setCancelled(true);
        tournament.setProcessed(true);

        log.warn("📨 SENDING CANCELLED notifications...");
        sendCancelledNotifications(notifications);

        repo.saveAll(notifications);

        log.warn("❌ tournament cancelled DONE: id={}, users={}",
                tournament.getExternalId(),
                notifications.size());

        return true;
    }

    // 🏁 завершение
    private void handleFinished(Tournament tournament,
                                List<PlayerNotification> notifications,
                                Document document) throws Exception {

        if (!tournamentParser.isFinished(document)) {
            log.warn("⏭ SKIP: not finished yet");
            return;
        }

        log.warn("🏁 FINISH DETECTED → parsing results...");

        ResultService.ParsedResult parsed = resultService.calculateAll(document);

        log.warn("📊 parsed results size={}", parsed.getResults().size());

        processService.processTournament(notifications, parsed);

        tournament.setFinished(true);
        tournament.setProcessed(true);

        repo.saveAll(notifications);

        log.warn("✅ tournament finished DONE: id={}, users={}",
                tournament.getExternalId(),
                notifications.size());
    }

    // 📩 отмена
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

            if (telegramId == null) {
                log.warn("❌ SKIP cancel send: no telegramId for notification={}", pn.getId());
                continue;
            }

            try {
                log.warn("📨 SEND CANCELLED → telegramId={}, tournament={}",
                        telegramId,
                        pn.getTournament().getExternalId());

                notificationService.send(
                        telegramId,
                        cancelledMessageBuilder.build(pn)
                );

                log.warn("✅ SENT CANCELLED → telegramId={}", telegramId);

            } catch (Exception e) {
                log.error("❌ CANCEL SEND FAILED → telegramId={}", telegramId, e);
            }
        }
    }
}