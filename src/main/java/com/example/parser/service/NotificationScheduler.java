package com.example.parser.service;

import com.example.parser.bot.BotHolder;
import com.example.parser.domain.entity.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationScheduler {

    private final PlayerService playerService;
    private final NotificationService notificationService;
    private final BotHolder botHolder;

    @Scheduled(fixedDelay = 100000) // каждые 5 минут
    public void checkAllUsers() {

        System.out.println("⏰ SCHEDULER START");

        List<Player> players = playerService.getAll();

        for (Player player : players) {
            try {
                notificationService.notifyUser(
                        player.getTelegramId(),
                        botHolder.getBot()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}