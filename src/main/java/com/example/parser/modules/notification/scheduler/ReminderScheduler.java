package com.example.parser.modules.notification.scheduler;

import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.notification.repository.PlayerNotificationRepository;
import com.example.parser.modules.tournament.persistence.entity.TournamentEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final PlayerNotificationRepository notificationRepo;
    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    @Scheduled(fixedRate = 60000, initialDelay = 30000)
    @Transactional
    public void checkReminders() {
        ZonedDateTime now = ZonedDateTime.now(ZONE).withSecond(0).withNano(0);
        List<PlayerNotification> list = notificationRepo.findAll();
        int sent = 0;

        for (PlayerNotification pn : list) {
            if (processNotification(pn, now)) sent++;
        }
        if (sent > 0) log.info("📩 reminders processed: {}", sent);
    }

    private boolean processNotification(PlayerNotification pn, ZonedDateTime now) {
        if (!isValid(pn)) return false;
        ZonedDateTime tournamentTime = buildTournamentTime(pn);
        if (now.isAfter(tournamentTime.plusHours(3))) return false;

        boolean changed = false;

        // Вечернее напоминание (21:00 накануне)
        if (tournamentTime.getHour() < 8 && !pn.isEveningSent()) {
            ZonedDateTime eveningTime = tournamentTime.minusDays(1).withHour(21).withMinute(0).withSecond(0).withNano(0);
            if (!now.isBefore(eveningTime)) {
                log.info("🌙 Evening reminder: player={}, tournament={}", pn.getPlayer().getId(), pn.getTournament().getId());
                pn.setEveningSent(true);
                changed = true;
            }
        }

        // Часовое напоминание
        if (!pn.isReminderSent()) {
            ZonedDateTime hourTime = tournamentTime.minusHours(1);
            if (!now.isBefore(hourTime)) {
                log.info("⏰ Hour reminder: player={}, tournament={}", pn.getPlayer().getId(), pn.getTournament().getId());
                pn.setReminderSent(true);
                changed = true;
            }
        }

        if (changed) notificationRepo.save(pn);
        return changed;
    }

    private boolean isValid(PlayerNotification pn) {
        TournamentEntity t = pn.getTournament();
        return t != null && t.getDate() != null && t.getTime() != null;
    }

    private ZonedDateTime buildTournamentTime(PlayerNotification pn) {
        TournamentEntity t = pn.getTournament();
        return ZonedDateTime.of(t.getDate(), LocalTime.parse(t.getTime()), ZONE).withSecond(0).withNano(0);
    }
}