package com.example.parser.service;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.repository.PlayerNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentProcessor {

    private final PlayerNotificationRepository repo;

    @Scheduled(fixedDelay = 60000)
    public void processTodayTournaments() {

        LocalDate today = LocalDate.now();

        List<PlayerNotification> list =
                repo.findAllByProcessedFalse();

        for (PlayerNotification pn : list) {

            if (pn.getDate() == null) continue;

            if (pn.getDate().isEqual(today)) {

                // 💥 ТУТ БУДЕТ ПАРСИНГ СТРАНИЦЫ
                System.out.println("🔥 PROCESS: " + pn.getLink());

                pn.setProcessed(true);
                repo.save(pn);
            }
        }
    }
}