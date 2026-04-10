package com.example.parser.notification;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.parser.ParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentStartScheduler {

    private final PlayerNotificationRepository repo;
    private final NotificationService notificationService;
    private final ParserService parserService;

    @Scheduled(fixedRate = 180000)
    public void checkStart() {

        long start = System.currentTimeMillis();   // 👈 ВОТ ЭТО
        log.warn("🔥 START checkStart");           // 👈 ВОТ ЭТО

        log.info("checkStart triggered");
        log.info("checkStart triggered");

        List<PlayerNotification> list = repo.findByStartedFalse();
        log.info("pending tournaments count={}", list.size());

        LocalDate today = LocalDate.now();

        // 🔥 группируем по турниру
        Map<String, List<PlayerNotification>> grouped =
                list.stream()
                        .collect(Collectors.groupingBy(PlayerNotification::getLink));

        for (var entry : grouped.entrySet()) {

            String link = entry.getKey();
            List<PlayerNotification> notifications = entry.getValue();

            try {

                if (link == null) {
                    log.warn("skip: null link");
                    continue;
                }


                PlayerNotification sample = notifications.get(0);

                if (sample.getDate() != null && !sample.getDate().isEqual(today)) {
                    continue;
                }

                // 🔥 один раз проверяем старт
                boolean started = parserService.isTournamentStarted(link);

                if (!started) {
                    continue;
                }

                // 🔁 всем игрокам этого турнира
                for (PlayerNotification pn : notifications) {

                    notificationService.send(
                            pn.getTelegramId(),
                            buildStartMessage(pn)
                    );

                    pn.setStarted(true);
                    repo.save(pn);
                }


                log.info("tournament started: id={}, link={}",
                        sample.getTournamentId(), link);


            } catch (Exception e) {
                log.error("failed to process tournament: link={}", link, e);
            }
            log.warn("🔥 END checkStart {} ms", System.currentTimeMillis() - start); // 👈 ВОТ ЭТО

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