package com.example.parser.modules.notification.scheduler;

import com.example.parser.modules.notification.discovery.TournamentDiscoveryService;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

//Чем занимается:
//🔍 Периодически проверяет новые турниры для всех пользователей и запускает их поиск
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final PlayerService playerService;
    private final TournamentDiscoveryService discoveryService;

    @Scheduled(fixedDelay = 600000)
    public void checkAllUsers() {
        log.info("⏰ Scheduler started");


        List<Player> players = playerService.getAll();


        for (Player player : players) {
            Long telegramId = player.getTelegramId();

            try {
                log.info("🚀 START parsing for user {}", telegramId);

                discoveryService.checkNewTournaments(telegramId);


            } catch (Exception e) {
                log.error("❌ Error while processing user telegramId={}", telegramId, e);
            }
        }

    }
}