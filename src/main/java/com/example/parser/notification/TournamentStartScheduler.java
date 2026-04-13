package com.example.parser.notification;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.domain.entity.Tournament;
import com.example.parser.notification.formatter.TournamentStartMessageBuilder;
import com.example.parser.parser.ParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Scheduled(fixedRate = 180000, initialDelay = 30000)
    @Transactional
    public void checkStart() {
        List<PlayerNotification> notifications = repo.findPendingWithTournament();

        Map<String, List<PlayerNotification>> grouped = notifications.stream()
                .filter(p -> p.getTournament() != null)
                .collect(Collectors.groupingBy(p -> p.getTournament().getLink()));

        grouped.forEach(this::processTournament);
    }

    private void processTournament(String link, List<PlayerNotification> notifications) {
        try {
            if (link == null) return;

            Tournament tournament = notifications.get(0).getTournament();
            if (tournament == null) return;

            // уже отправляли
            if (tournament.isStarted()) return;

            if (!isToday(tournament)) return;

            boolean startedByParser = parserService.isTournamentStarted(link);
            boolean startedByTime = isStartedByTime(tournament);

            if (!startedByParser && !startedByTime) return;

            // отправка
            sendNotifications(notifications);

            // 🔥 фиксируем
            tournament.setStarted(true);
            repo.saveAll(notifications);

            log.info("🚀 tournament started: id={}, users={}",
                    tournament.getExternalId(),
                    notifications.size());

        } catch (Exception e) {
            log.error("failed to process tournament: link={}", link, e);
        }
    }

    private boolean isStartedByTime(Tournament t) {
        if (t.getDate() == null || t.getTime() == null) return false;

        ZonedDateTime start = ZonedDateTime.of(
                t.getDate(),
                LocalTime.parse(t.getTime()),
                ZoneId.of("Europe/Moscow")
        );

        return ZonedDateTime.now(ZoneId.of("Europe/Moscow")).isAfter(start);
    }

    private boolean isToday(Tournament t) {
        return t.getDate() != null && t.getDate().isEqual(LocalDate.now());
    }

    private void sendNotifications(List<PlayerNotification> notifications) {
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
                        startMessageBuilder.build(pn)
                );
            } catch (Exception e) {
                log.error("send failed: {}", telegramId, e);
            }
        }
    }
}