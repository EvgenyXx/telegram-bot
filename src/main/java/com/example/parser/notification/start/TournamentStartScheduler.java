package com.example.parser.notification.start;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.notification.PlayerNotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentStartScheduler {

    private final PlayerNotificationRepository repo;
    private final TournamentProcessor processor;

    @Scheduled(fixedRate = 180000)
    @Transactional
    public void checkStart() {
        List<PlayerNotification> notifications = repo.findPendingWithTournament();

        log.info("🔄 StartScheduler tick: totalNotifications={}", notifications.size());

        Map<String, List<PlayerNotification>> grouped = notifications.stream()
                .filter(p -> p.getTournament() != null)
                .collect(Collectors.groupingBy(p -> p.getTournament().getLink()));

        int processed = 0;

        for (Map.Entry<String, List<PlayerNotification>> entry : grouped.entrySet()) {
            processor.process(entry.getKey(), entry.getValue());
            processed++;
        }

        log.info("✅ StartScheduler done: processed={}", processed);
    }
}