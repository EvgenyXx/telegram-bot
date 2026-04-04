package com.example.parser.notification;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.parser.ParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

//Чем занимается:
//🚀 Проверяет начался ли турнир для игрока и отправляет уведомление о старте
@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentStartScheduler {

    private final PlayerNotificationRepository repo;
    private final NotificationService notificationService;
    private final ParserService parserService;

    @Scheduled(fixedRate = 180000)// повысить время
    public void checkStart() {

        List<PlayerNotification> list = repo.findByStartedFalse();

        for (PlayerNotification pn : list) {
            try {
                if (pn.getLink() == null) continue;

                // ✅ ФИЛЬТР ПО ДАТЕ (ТОЛЬКО СЕГОДНЯ)
                if (pn.getDate() != null) {
                    if (!pn.getDate().isEqual(LocalDate.now())) {
                        continue;
                    }
                }



                boolean started = parserService.isTournamentStarted(pn.getLink());


                if (!started) continue;

                // 🚀 уведомление
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
        return "🚀 Турнир начался!\n\n"
                + "📅 Дата: " + pn.getDate() + "\n"
                + "⏰ Время: " + pn.getTime() + "\n"
                + "🔗 " + pn.getLink() + "\n\n"
                + "📊 Результаты будут автоматически посчитаны и добавлены в твои турниры";
    }
}