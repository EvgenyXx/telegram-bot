package com.example.parser.notification;

import com.example.parser.bot.BotHolder;
import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.domain.entity.Tournament;
import com.example.parser.notification.formatter.ReminderMessageBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final PlayerNotificationRepository notificationRepo;
    private final MessageService messageService;
    private final BotHolder botHolder;
    private final ReminderMessageBuilder messageBuilder;

    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    @Scheduled(fixedRate = 60000, initialDelay = 30000)
    @Transactional
    public void checkReminders() {
        TelegramLongPollingBot bot = botHolder.getBot();
        if (bot == null) return;

        ZonedDateTime now = ZonedDateTime.now(ZONE).withSecond(0).withNano(0);
        List<PlayerNotification> list = notificationRepo.findAll();

        int sentHour = 0;
        int sentEvening = 0;

        for (PlayerNotification pn : list) {
            Result r = processNotification(pn, now, bot);
            if (r == null) continue;

            if (r.hourSent) sentHour++;
            if (r.eveningSent) sentEvening++;
        }

        if (sentHour > 0 || sentEvening > 0) {
            log.info("📩 reminders sent: hour={}, evening={}", sentHour, sentEvening);
        }
    }

    private Result processNotification(PlayerNotification pn,
                                       ZonedDateTime now,
                                       TelegramLongPollingBot bot) {

        if (!isValid(pn)) return null;

        ZonedDateTime tournamentTime = buildTournamentTime(pn);

        // мягкий лимит — после турнира +3 часа уже не шлём
        if (now.isAfter(tournamentTime.plusHours(3))) return null;

        boolean evening = processEveningReminder(pn, now, tournamentTime, bot);
        boolean hour = processHourReminder(pn, now, tournamentTime, bot);

        return new Result(hour, evening);
    }

    // ================== EVENING ==================
    private boolean processEveningReminder(PlayerNotification pn,
                                           ZonedDateTime now,
                                           ZonedDateTime tournamentTime,
                                           TelegramLongPollingBot bot) {

        if (tournamentTime.getHour() >= 8) return false;
        if (pn.isEveningSent()) return false;

        ZonedDateTime eveningTime = tournamentTime
                .minusDays(1)
                .withHour(21)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        if (!now.isBefore(eveningTime)) {
            try {
                sendEveningReminder(pn, bot);
                pn.setEveningSent(true);
                notificationRepo.save(pn);
                return true;
            } catch (Exception e) {
                log.error("❌ evening send failed: user={}",
                        pn.getPlayer().getTelegramId(), e);
            }
        }

        return false;
    }

    // ================== HOUR ==================
    private boolean processHourReminder(PlayerNotification pn,
                                        ZonedDateTime now,
                                        ZonedDateTime tournamentTime,
                                        TelegramLongPollingBot bot) {

        if (pn.isReminderSent()) return false;

        ZonedDateTime hourTime = tournamentTime.minusHours(1);

        if (!now.isBefore(hourTime)) {
            try {
                sendHourReminder(pn, bot);
                pn.setReminderSent(true);
                notificationRepo.save(pn);
                return true;
            } catch (Exception e) {
                log.error("❌ hour send failed: user={}",
                        pn.getPlayer().getTelegramId(), e);
            }
        }

        return false;
    }

    // ================== SEND ==================
    private void sendEveningReminder(PlayerNotification pn,
                                     TelegramLongPollingBot bot) {

        messageService.send(
                bot,
                pn.getPlayer().getTelegramId(),
                messageBuilder.buildEvening(pn)
        );
    }

    private void sendHourReminder(PlayerNotification pn,
                                  TelegramLongPollingBot bot) {

        messageService.send(
                bot,
                pn.getPlayer().getTelegramId(),
                messageBuilder.buildHour(pn)
        );
    }

    // ================== VALIDATION ==================
    private boolean isValid(PlayerNotification pn) {
        Tournament t = pn.getTournament();
        return t != null && t.getDate() != null && t.getTime() != null;
    }

    private ZonedDateTime buildTournamentTime(PlayerNotification pn) {
        Tournament t = pn.getTournament();

        return ZonedDateTime.of(
                t.getDate(),
                LocalTime.parse(t.getTime()),
                ZONE
        ).withSecond(0).withNano(0);
    }

    private record Result(boolean hourSent, boolean eveningSent) {}
}