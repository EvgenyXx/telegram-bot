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
        TelegramLongPollingBot bot = getBot();
        if (bot == null) return;

        ZonedDateTime now = ZonedDateTime.now(ZONE).withSecond(0).withNano(0);

        List<PlayerNotification> list = notificationRepo.findAll();
        log.debug("checkReminders: total notifications={}", list.size());

        for (PlayerNotification pn : list) {
            processNotification(pn, now, bot);
        }
    }

    private TelegramLongPollingBot getBot() {
        TelegramLongPollingBot bot = botHolder.getBot();
        if (bot == null) {
            log.warn("bot is not initialized yet");
        }
        return bot;
    }

    private void processNotification(PlayerNotification pn,
                                     ZonedDateTime now,
                                     TelegramLongPollingBot bot) {

        if (!isValid(pn)) return;

        ZonedDateTime tournamentTime = buildTournamentTime(pn);

        // 🔥 не обрабатываем прошедшие турниры
        if (now.isAfter(tournamentTime)) return;

        processEveningReminder(pn, now, tournamentTime, bot);
        processHourReminder(pn, now, tournamentTime, bot);
    }

    // ================== EVENING ==================

    private void processEveningReminder(PlayerNotification pn,
                                        ZonedDateTime now,
                                        ZonedDateTime tournamentTime,
                                        TelegramLongPollingBot bot) {

        if (tournamentTime.getHour() >= 8) return;
        if (pn.isEveningSent()) return;

        ZonedDateTime eveningTime = tournamentTime
                .minusDays(1)
                .withHour(21)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);

        if (now.isAfter(eveningTime)) {

            sendEveningReminder(pn, bot);

            pn.setEveningSent(true);
            notificationRepo.save(pn);

            log.info("🌙 evening reminder sent: user={}, tournament={}",
                    pn.getPlayer().getTelegramId(),
                    pn.getTournament().getExternalId());
        }
    }

    // ================== HOUR ==================

    private void processHourReminder(PlayerNotification pn,
                                     ZonedDateTime now,
                                     ZonedDateTime tournamentTime,
                                     TelegramLongPollingBot bot) {

        log.warn("NOW={}, TOURNAMENT={}, HOUR_BEFORE={}, REMINDER_SENT={}",
                now,
                tournamentTime,
                tournamentTime.minusHours(1),
                pn.isReminderSent()
        );

        if (pn.isReminderSent()) return;

        ZonedDateTime hourTime = tournamentTime.minusHours(1);

        if (now.isAfter(hourTime)) {
            sendHourReminder(pn, bot);
            pn.setReminderSent(true);
            notificationRepo.save(pn);

        }
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
}