package com.example.parser.notification;

import com.example.parser.bot.BotHolder;
import com.example.parser.domain.entity.Player;
import com.example.parser.player.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final PlayerService playerService;
    private final NotificationService notificationService;
    private final BotHolder botHolder;

    @Scheduled(fixedDelay = 300000) // 5 минут
    public void checkAllUsers() {

        log.info("⏰ Scheduler started");

        List<Player> players = playerService.getAll();


        for (Player player : players) {

            Long telegramId = player.getTelegramId();

            try {
                log.debug("➡️ Processing user: telegramId={}", telegramId);

                notificationService.notifyUser(
                        telegramId,
                        botHolder.getBot()
                );

            } catch (Exception e) {
                log.error("❌ Error while processing user telegramId={}",
                        telegramId, e);
            }
        }

        log.info("✅ Scheduler finished");
    }
}