package com.example.parser.notification;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.domain.entity.Tournament;
import com.example.parser.integration.DocumentLoader;
import com.example.parser.notification.formatter.TournamentCancelledMessageBuilder;
import com.example.parser.notification.formatter.TournamentStartMessageBuilder;
import com.example.parser.parser.ParserService;
import com.example.parser.tournament.parser.TournamentParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentStartScheduler {

    private final PlayerNotificationRepository repo;
    private final NotificationService notificationService;
    private final ParserService parserService;
    private final TournamentStartMessageBuilder startMessageBuilder;
    private final DocumentLoader documentLoader;
    private final TournamentParser tournamentParser;
    private final TournamentCancelledMessageBuilder cancelledMessageBuilder;

    @Scheduled(fixedRate = 180000, initialDelay = 30000)
    @Transactional
    public void checkStart() {

        log.warn("🔄 SCHEDULER START");

        List<PlayerNotification> notifications = repo.findPendingWithTournament();

        log.warn("📊 total notifications={}", notifications.size());

        Map<String, List<PlayerNotification>> grouped = notifications.stream()
                .filter(p -> p.getTournament() != null)
                .collect(Collectors.groupingBy(p -> p.getTournament().getLink()));

        log.warn("📦 grouped tournaments={}", grouped.size());

        grouped.forEach(this::processTournament);
    }

    private void processTournament(String link, List<PlayerNotification> notifications) {

        log.warn("➡️ PROCESS tournament: link={}, users={}", link, notifications.size());

        try {
            if (link == null) {
                log.warn("❌ SKIP: link is null");
                return;
            }

            Tournament tournament = notifications.get(0).getTournament();

            if (tournament == null) {
                log.warn("❌ SKIP: tournament is null");
                return;
            }

            log.warn("📌 tournamentId={}, started={}, cancelled={}, date={}, time={}",
                    tournament.getExternalId(),
                    tournament.isStarted(),
                    tournament.isCancelled(),
                    tournament.getDate(),
                    tournament.getTime()
            );

            Document document = documentLoader.load(link);

            // ❌ ОТМЕНА
            if (tournamentParser.isCancelled(document)) {

                log.warn("⚠️ DETECTED CANCELLED: tournament={}", tournament.getExternalId());

                if (tournament.isCancelled()) {
                    log.warn("⏭ SKIP: already cancelled");
                    return;
                }

                tournament.setCancelled(true);

                log.warn("📨 SENDING CANCELLED notifications...");
                sendCancelledNotifications(notifications);

                repo.saveAll(notifications);

                log.warn("❌ tournament cancelled DONE: id={}, users={}",
                        tournament.getExternalId(),
                        notifications.size());

                return;
            }

            if (tournament.isStarted()) {
                log.warn("⏭ SKIP: already started");
                return;
            }

            if (!isToday(tournament)) {
                log.warn("⏭ SKIP: not today");
                return;
            }

            boolean startedByParser = parserService.isTournamentStarted(link);
            boolean startedByTime = isStartedByTime(tournament);

            log.warn("🔍 CHECK start: parser={}, time={}", startedByParser, startedByTime);

            if (!startedByParser && !startedByTime) {
                log.warn("⏭ SKIP: not started yet");
                return;
            }

            // 🚀 СТАРТ
            log.warn("🚀 START DETECTED → sending notifications...");

            sendStartNotifications(notifications);

            tournament.setStarted(true);

            repo.saveAll(notifications);

            log.warn("✅ tournament started DONE: id={}, users={}",
                    tournament.getExternalId(),
                    notifications.size());

        } catch (Exception e) {
            log.error("❌ FAILED processing tournament: link={}", link, e);
        }
    }

    private boolean isStartedByTime(Tournament t) {

        if (t.getDate() == null || t.getTime() == null) {
            log.warn("⚠️ isStartedByTime SKIP: no date/time");
            return false;
        }

        ZonedDateTime start = ZonedDateTime.of(
                t.getDate(),
                LocalTime.parse(t.getTime()),
                ZoneId.of("Europe/Moscow")
        );

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));

        log.warn("⏰ TIME CHECK: now={}, start={}", now, start);

        return now.isAfter(start);
    }

    private boolean isToday(Tournament t) {

        boolean result = t.getDate() != null && t.getDate().isEqual(LocalDate.now());

        log.warn("📅 isToday={} (date={})", result, t.getDate());

        return result;
    }

    // 🚀 старт
    private void sendStartNotifications(List<PlayerNotification> notifications) {

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
                log.warn("❌ SKIP send: no telegramId for notification={}", pn.getId());
                continue;
            }

            try {
                log.warn("📨 SEND START → telegramId={}, tournament={}",
                        telegramId,
                        pn.getTournament().getExternalId());

                notificationService.send(
                        telegramId,
                        startMessageBuilder.build(pn)
                );

                log.warn("✅ SENT START → telegramId={}", telegramId);

            } catch (Exception e) {
                log.error("❌ START SEND FAILED → telegramId={}", telegramId, e);
            }
        }
    }

    // ❌ отмена
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