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

    @Scheduled(fixedRate = 60000) // каждую минуту
    public void checkReminders() {

        TelegramLongPollingBot bot = botHolder.getBot();
        if (bot == null) {
            log.warn("❌ Bot is not initialized yet");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<PlayerNotification> list = notificationRepo.findByReminderSentFalse();

        for (PlayerNotification pn : list) {

            if (pn.getDate() == null || pn.getTime() == null) continue;

            LocalDateTime tournamentTime = LocalDateTime.of(
                    pn.getDate(),
                    LocalTime.parse(pn.getTime())
            );

            LocalDateTime reminderTime = tournamentTime.minusHours(3);

            if (now.isAfter(reminderTime) && now.isBefore(tournamentTime)) {

                String msg = "⏰ Напоминание\n\n"
                        + "Через 3 часа турнир\n\n"
                        + "📅 " + pn.getDate() + "\n"
                        + "🕒 " + pn.getTime() + "\n"
                        + "🔗 " + pn.getLink();

                messageService.send(bot, pn.getTelegramId(), msg);

                pn.setReminderSent(true);
                notificationRepo.save(pn);

                log.info("✅ Reminder sent: tournamentId={}", pn.getTournamentId());
            }
        }
    }
}