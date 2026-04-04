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

    @Scheduled(fixedRate = 60000)
    public void checkReminders() {
        TelegramLongPollingBot bot = getBot();
        if (bot == null) return;

        ZonedDateTime now = ZonedDateTime.now(ZONE);
        List<PlayerNotification> list = notificationRepo.findByReminderSentFalse();

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
        ZonedDateTime reminderTime = buildReminderTime(tournamentTime);

        if (shouldSend(pn, now, reminderTime, tournamentTime)) {
            sendReminder(pn, bot);
        }
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