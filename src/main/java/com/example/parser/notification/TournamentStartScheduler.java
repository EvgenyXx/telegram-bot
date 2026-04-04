package com.example.parser.notification;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.parser.ParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


// 🚀 Проверяет, начался ли турнир (по первому матчу),
// отправляет уведомление один раз и обновляет статус
@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentStartScheduler {

    private final PlayerNotificationRepository repo;
    private final NotificationService notificationService;
    private final ParserService parserService;

    @Scheduled(fixedRate = 180000)
    public void checkStart() {

        log.info("checkStart triggered");

        List<PlayerNotification> list = repo.findByStartedFalse();

        log.info("pending tournaments count={}", list.size());

        LocalDate today = LocalDate.now();

        for (PlayerNotification pn : list) {
            try {

                if (pn.getLink() == null) {
                    log.warn("skip: null link, tournamentId={}", pn.getTournamentId());
                    continue;
                }

                if (pn.getDate() != null && !pn.getDate().isEqual(today)) {
                    continue;
                }

                boolean started = parserService.isTournamentStarted(pn.getLink());

                if (!started) {
                    continue;
                }

                notificationService.send(pn.getTelegramId(), buildStartMessage(pn));

                pn.setStarted(true);
                repo.save(pn);

                log.info("tournament started: id={}, link={}",
                        pn.getTournamentId(), pn.getLink());

            } catch (Exception e) {
                log.error("failed to process tournament: link={}", pn.getLink(), e);
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