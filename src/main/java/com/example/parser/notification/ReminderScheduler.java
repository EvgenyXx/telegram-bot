package com.example.parser.notification;

import com.example.parser.bot.BotHolder;
import com.example.parser.domain.entity.PlayerNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.time.*;
import java.util.List;

// ⏰ Отправляет напоминание за 1 час до начала турнира
// Каждую минуту проверяет PlayerNotification:
// если время подошло и reminder ещё не отправлен → шлёт сообщение и ставит флаг
//
// ⚠️ Лучше использовать findByReminderSentFalse() вместо findAll()
@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final PlayerNotificationRepository notificationRepo;
    private final MessageService messageService;
    private final BotHolder botHolder;

    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    @Scheduled(fixedRate = 60000,initialDelay = 30000)
    public void checkReminders() {

        long start = System.currentTimeMillis();   // 👈 ДОБАВЬ
        log.warn("🔥 START checkReminders");       // 👈 ДОБАВЬ
        TelegramLongPollingBot bot = getBot();
        if (bot == null) return;

        ZonedDateTime now = ZonedDateTime.now(ZONE);
        List<PlayerNotification> list = notificationRepo.findAll();


        for (PlayerNotification pn : list) {
            processNotification(pn, now, bot);
        }



    }

    private TelegramLongPollingBot getBot() {
        TelegramLongPollingBot bot = botHolder.getBot();
        if (bot == null) {
            log.warn("❌ Bot is not initialized yet");
        }
        return bot;
    }

    private void processNotification(PlayerNotification pn,
                                     ZonedDateTime now,
                                     TelegramLongPollingBot bot) {

        if (!isValid(pn)) return;

        ZonedDateTime tournamentTime = buildTournamentTime(pn);

        // 🌙 вечернее (только для ранних турниров)
        // 🌙 вечернее
        if (tournamentTime.getHour() < 8 && !Boolean.TRUE.equals(pn.getEveningSent())) {

            ZonedDateTime eveningTime = tournamentTime
                    .minusDays(1)
                    .withHour(21)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);

            if (now.isAfter(eveningTime) && now.isBefore(tournamentTime)) {
                sendEveningReminder(pn, bot);
                pn.setEveningSent(true);
                notificationRepo.save(pn);
            }
        }

// ⏰ за час
        if (!Boolean.TRUE.equals(pn.getReminderSent())) {

            ZonedDateTime hourTime = tournamentTime.minusHours(1);

            if (now.isAfter(hourTime) && now.isBefore(tournamentTime)) {
                sendHourReminder(pn, bot);
                pn.setReminderSent(true);
                notificationRepo.save(pn);
            }
        }
    }

    private void sendEveningReminder(PlayerNotification pn, TelegramLongPollingBot bot) {
        String msg = "🌙 Напоминание на завтра\n\n" +
                "🏓 У тебя турнир утром\n" +
                "📅 " + pn.getDate() + "\n" +
                "🕒 " + pn.getTime() + "\n\n" +
                "Подготовься заранее 💪";

        messageService.send(bot, pn.getTelegramId(), msg);
    }

    private void sendHourReminder(PlayerNotification pn, TelegramLongPollingBot bot) {
        String msg = "⏰ Через 1 час турнир\n\n" +
                "📅 " + pn.getDate() + "\n" +
                "🕒 " + pn.getTime() + "\n" +
                "🔗 " + pn.getLink();

        messageService.send(bot, pn.getTelegramId(), msg);
    }

    private boolean isValid(PlayerNotification pn) {
        return pn.getDate() != null && pn.getTime() != null;
    }

    private ZonedDateTime buildTournamentTime(PlayerNotification pn) {
        return ZonedDateTime.of(
                pn.getDate(),
                LocalTime.parse(pn.getTime()),
                ZONE
        );
    }

    private ZonedDateTime buildReminderTime(ZonedDateTime tournamentTime) {
        return tournamentTime.minusHours(1);
    }

    private boolean shouldSend(PlayerNotification pn,
                               ZonedDateTime now,
                               ZonedDateTime reminderTime,
                               ZonedDateTime tournamentTime) {

        return !Boolean.TRUE.equals(pn.getReminderSent())
                && now.isAfter(reminderTime)
                && now.isBefore(tournamentTime);
    }

    private void sendReminder(PlayerNotification pn, TelegramLongPollingBot bot) {
        String msg = buildMessage(pn);

        messageService.send(bot, pn.getTelegramId(), msg);

        pn.setReminderSent(true);
        notificationRepo.save(pn);

        log.warn("🔥 REMINDER SENT: {}", pn.getTournamentId());
    }

    private String buildMessage(PlayerNotification pn) {
        return "⏰ Напоминание\n\n"
                + "Через 1 час турнир\n\n"
                + "📅 " + pn.getDate() + "\n"
                + "🕒 " + pn.getTime() + "\n"
                + "🔗 " + pn.getLink();
    }
}