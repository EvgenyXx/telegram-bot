package com.example.parser.notification;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.tournament.TournamentProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentStartScheduler {

    private final PlayerNotificationRepository notificationRepo;
    private final TournamentProcessor tournamentProcessor;

    @Scheduled(fixedRate = 60000) // каждую минуту
    public void checkTournamentStart() {

        LocalDateTime now = LocalDateTime.now();

        List<PlayerNotification> list =
                notificationRepo.findByStartedFalse();

        for (PlayerNotification pn : list) {

            if (pn.getDate() == null || pn.getTime() == null) continue;

            LocalDateTime tournamentTime = LocalDateTime.of(
                    pn.getDate(),
                    LocalTime.parse(pn.getTime())
            );

            // можно сделать с запасом (например ±10 минут)
            if (now.isAfter(tournamentTime.minusMinutes(5))) {

                log.info("🚀 Tournament started: {}", pn.getLink());

                // 👉 вот тут начинается магия
                tournamentProcessor.process(pn);

                pn.setStarted(true);
                notificationRepo.save(pn);
            }
        }
    }


}