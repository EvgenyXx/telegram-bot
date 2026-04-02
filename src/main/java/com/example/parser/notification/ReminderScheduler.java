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

        TelegramLongPollingBot bot = botHolder.getBot();
        if (bot == null) {
            log.warn("❌ Bot is not initialized yet");
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(ZONE);

        List<PlayerNotification> list = notificationRepo.findByReminderSentFalse();

        for (PlayerNotification pn : list) {

            if (pn.getDate() == null || pn.getTime() == null) continue;

            ZonedDateTime tournamentTime = ZonedDateTime.of(
                    pn.getDate(),
                    LocalTime.parse(pn.getTime()),
                    ZONE
            );

            ZonedDateTime reminderTime = tournamentTime.minusHours(1);

            // 🔍 ЛОГ ДЛЯ ПРОВЕРКИ
            log.info("NOW: {}", now);
            log.info("REMINDER: {}", reminderTime);
            log.info("TOURNAMENT: {}", tournamentTime);

            if (now.isAfter(reminderTime) && now.isBefore(tournamentTime)) {

                String msg = "⏰ Напоминание\n\n" +
                        "Через 1 час турнир\n\n" +
                        "📅 " + pn.getDate() + "\n" +
                        "🕒 " + pn.getTime() + "\n" +
                        "🔗 " + pn.getLink();

                messageService.send(bot, pn.getTelegramId(), msg);

                pn.setReminderSent(true);
                notificationRepo.save(pn);

                log.info("✅ Reminder sent: tournamentId={}", pn.getTournamentId());
            }
        }
    }
}