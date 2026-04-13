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

        log.debug("checkStart triggered");

        List<PlayerNotification> notifications = repo.findPendingWithTournament();
        log.info("pending tournaments count={}", notifications.size());

        Map<String, List<PlayerNotification>> grouped = notifications.stream()
                .filter(p -> p.getTournament() != null)
                .collect(Collectors.groupingBy(p -> p.getTournament().getLink()));

        grouped.forEach(this::processTournament);
    }

    private void processTournament(String link, List<PlayerNotification> notifications) {

        try {
            if (link == null) {
                log.warn("skip: null link");
                return;
            }

            Tournament tournament = notifications.get(0).getTournament();

            if (tournament == null) {
                log.warn("skip: tournament is null");
                return;
            }

            // 🔥 уже отправляли — выходим
            if (tournament.isStarted()) {
                return;
            }

            if (!isToday(tournament)) return;

            if (!parserService.isTournamentStarted(link)) return;

            // 🔥 отправка
            sendNotifications(notifications);

            // 🔥 фиксируем ОДИН раз
            tournament.setStarted(true);

            log.info("🚀 tournament started: id={}, link={}, users={}",
                    tournament.getExternalId(),
                    link,
                    notifications.size());

        } catch (Exception e) {
            log.error("failed to process tournament: link={}", link, e);
        }
    }

    private boolean isToday(Tournament t) {
        return t.getDate() == null || t.getDate().isEqual(LocalDate.now());
    }

    private void sendNotifications(List<PlayerNotification> notifications) {

        List<Long> ids = notifications.stream()
                .map(PlayerNotification::getId)
                .toList();

        Map<Long, Long> telegramMap = repo.findTelegramIdsByNotificationIds(ids)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1],
                        (a, b) -> a
                ));

        for (PlayerNotification pn : notifications) {

            Long telegramId = telegramMap.get(pn.getId());

            if (telegramId == null) {
                log.warn("telegramId not found for pn={}", pn.getId());
                continue;
            }

            try {
                notificationService.send(
                        telegramId,
                        startMessageBuilder.build(pn)
                );

            } catch (Exception e) {
                log.error("❌ FAILED SEND: telegramId={}", telegramId, e);

                if (e.getMessage() != null && e.getMessage().contains("bot was blocked")) {
                    log.warn("🚫 USER BLOCKED BOT: telegramId={}", telegramId);
                }
            }
        }
    }
}