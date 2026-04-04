package com.example.parser.notification;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.parser.ParserService;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

//Чем занимается:
//🚀 Проверяет начался ли турнир для игрока и отправляет уведомление о старте
@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentStartScheduler {

    private final PlayerNotificationRepository repo;
    private final NotificationService notificationService;
    private final PlayerService playerService;
    private final ParserService parserService;

    @Scheduled(fixedRate = 60000)
    public void checkStart() {
        List<PlayerNotification> list = repo.findByStartedFalse();

        for (PlayerNotification pn : list) {
            try {
                if (pn.getLink() == null) continue;

                Player player = playerService.getByTelegramId(pn.getTelegramId());
                if (player == null) continue;

                boolean started = parserService.isStartedForPlayer(
                        pn.getLink(),
                        player.getName()
                );

                if (!started) continue;

                // 🚀 ТОЛЬКО уведомление
                notificationService.send(
                        pn.getTelegramId(),
                        buildStartMessage(pn)
                );

                pn.setStarted(true);
                repo.save(pn);

                log.info("🚀 Tournament started: {}", pn.getTournamentId());

            } catch (Exception e) {
                log.error("❌ ERROR {}", pn.getLink(), e);
            }
        }
    }



    private String buildStartMessage(PlayerNotification pn) {
        return "🚀 Турнир начался!\n\n" +
                "📅 Дата: " + pn.getDate() + "\n" +
                "⏰ Время: " + pn.getTime() + "\n" +
                "🔗 " + pn.getLink() + "\n\n" +
                "📊 Результаты будут автоматически посчитаны и добавлены в твои турниры";
    }
}