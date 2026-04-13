package com.example.parser.notification;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.domain.entity.Tournament;
import com.example.parser.integration.DocumentLoader;
import com.example.parser.notification.formatter.TournamentCancelledMessageBuilder;
import com.example.parser.notification.formatter.TournamentStartMessageBuilder;
import com.example.parser.parser.ParserService;
import com.example.parser.tournament.parser.TournamentParser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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

    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    @Scheduled(fixedRate = 180000)
    @Transactional
    public void checkStart() {
        List<PlayerNotification> notifications = repo.findPendingWithTournament();

        log.info("🔄 StartScheduler tick: totalNotifications={}", notifications.size());

        Map<String, List<PlayerNotification>> grouped = notifications.stream()
                .filter(p -> p.getTournament() != null)
                .collect(Collectors.groupingBy(p -> p.getTournament().getLink()));

        int processed = 0;
        int started = 0;
        int cancelled = 0;

        for (Map.Entry<String, List<PlayerNotification>> entry : grouped.entrySet()) {
            Result r = processTournament(entry.getKey(), entry.getValue());
            if (r == null) continue;

            processed++;
            if (r.started) started++;
            if (r.cancelled) cancelled++;
        }

        log.info("✅ StartScheduler done: processed={}, started={}, cancelled={}",
                processed, started, cancelled);
    }

    private Result processTournament(String link, List<PlayerNotification> notifications) {
        if (link == null || notifications.isEmpty()) return null;

        try {
            Tournament tournament = notifications.get(0).getTournament();
            if (tournament == null) return null;

            Document document = documentLoader.load(link);

            // ❌ CANCELLED
            if (tournamentParser.isCancelled(document)) {
                if (tournament.isCancelled()) return null;

                tournament.setCancelled(true);
                sendCancelledNotifications(notifications);
                repo.saveAll(notifications);

                log.info("❌ tournament cancelled: id={}, users={}",
                        tournament.getExternalId(),
                        notifications.size());

                return new Result(false, true);
            }

            if (tournament.isStarted()) return null;

            // 🔥 FIX: правильная дата с зоной
            if (!isToday(tournament)) {
                log.info("⛔ skip (not today): id={}, dbDate={}, nowDate={}",
                        tournament.getExternalId(),
                        tournament.getDate(),
                        ZonedDateTime.now(ZONE).toLocalDate());
                return null;
            }

            boolean startedByParser = parserService.isTournamentStarted(link);
            boolean startedByTime = isStartedByTime(tournament);

            // 🔥 ВОТ ЭТО ТЕБЕ НУЖНО
            log.info("TIME CHECK: now={}, start={}",
                    ZonedDateTime.now(ZONE),
                    ZonedDateTime.of(
                            tournament.getDate(),
                            LocalTime.parse(tournament.getTime()),
                            ZONE
                    ));

            log.info("DEBUG start check: id={}, parser={}, time={}",
                    tournament.getExternalId(),
                    startedByParser,
                    startedByTime);

            if (isNearStart(tournament)) {
                log.info("⏰ start window: tournamentId={}, parser={}, time={}",
                        tournament.getExternalId(),
                        startedByParser,
                        startedByTime);
            }

            // 🔥 условие как надо
            if (!startedByParser && !startedByTime) return null;

            // 🚀 START
            int success = sendStartNotifications(notifications);

            if (success > 0) {
                tournament.setStarted(true);
                repo.saveAll(notifications);
            }

            log.info("🚀 tournament started: id={}, success={}",
                    tournament.getExternalId(),
                    success);

            return new Result(success > 0, false);

        } catch (Exception e) {
            log.error("❌ failed to process tournament: link={}", link, e);
            return null;
        }
    }

    private int sendStartNotifications(List<PlayerNotification> notifications) {
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
                log.info("📤 START SEND: telegramId={}", telegramId);

                notificationService.send(
                        telegramId,
                        startMessageBuilder.build(pn)
                );

                success++;

            } catch (Exception e) {
                failed++;
                log.error("❌ start send failed: telegramId={}", telegramId, e);
            }
        }

        log.info("📩 start notifications: success={}, failed={}", success, failed);
        return success;
    }

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

    private boolean isStartedByTime(Tournament t) {
        if (t.getDate() == null || t.getTime() == null) return false;

        ZonedDateTime start = ZonedDateTime.of(
                t.getDate(),
                LocalTime.parse(t.getTime()),
                ZONE
        );

        return ZonedDateTime.now(ZONE).isAfter(start);
    }//todo

    private boolean isToday(Tournament t) {
        return t.getDate() != null &&
                t.getDate().isEqual(ZonedDateTime.now(ZONE).toLocalDate());
    }

    private boolean isNearStart(Tournament t) {
        if (t.getDate() == null || t.getTime() == null) return false;

        ZonedDateTime now = ZonedDateTime.now(ZONE);
        ZonedDateTime start = ZonedDateTime.of(
                t.getDate(),
                LocalTime.parse(t.getTime()),
                ZONE
        );

        long minutes = Math.abs(java.time.Duration.between(now, start).toMinutes());
        return minutes <= 5;
    }

    private record Result(boolean started, boolean cancelled) {}
}