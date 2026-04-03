package com.example.parser.notification;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.tournament.TournamentProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

//@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentStartScheduler {

    private final PlayerNotificationRepository notificationRepo;
    private final TournamentProcessor tournamentProcessor;
    private final NotificationService notificationService;

    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

//    @Scheduled(fixedRate = 60000)
    public void checkTournamentStart() {

        ZonedDateTime now = ZonedDateTime.now(ZONE);

        log.error("🚀 START SCHEDULER RUNNING, now={}", now);

        List<PlayerNotification> list = notificationRepo.findByStartedFalse();

        for (PlayerNotification pn : list) {

            if (pn.getDate() == null || pn.getTime() == null) continue;

            ZonedDateTime tournamentTime = ZonedDateTime.of(
                    pn.getDate(),
                    LocalTime.parse(pn.getTime()),
                    ZONE
            );

            log.error("🧪 CHECK PN: id={}, now={}, tournamentTime={}",
                    pn.getTournamentId(),
                    now,
                    tournamentTime
            );

            boolean shouldStart = now.isAfter(tournamentTime.minusMinutes(5));

            log.error("⏱ CONDITION = {}", shouldStart);

            if (shouldStart) {

                log.warn("🔥 TOURNAMENT START TRIGGERED: {}", pn.getTournamentId());

                // ✅ уведомление
                notificationService.sendTournamentStarted(pn);

                // ✅ запуск парсинга
                tournamentProcessor.process(pn);

                // ✅ ставим флаг
                pn.setStarted(true);
                notificationRepo.save(pn);

                log.warn("✅ STARTED SAVED: {}", pn.getTournamentId());
            }
        }
    }
}