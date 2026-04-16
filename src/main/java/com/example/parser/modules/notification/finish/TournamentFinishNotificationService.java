package com.example.parser.modules.notification.finish;

import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.notification.service.NotificationService;
import com.example.parser.modules.notification.repository.PlayerNotificationRepository;
import com.example.parser.modules.notification.formatter.TournamentCancelledMessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentFinishNotificationService {

    private final PlayerNotificationRepository repo;
    private final NotificationService notificationService;
    private final TournamentCancelledMessageBuilder cancelBuilder;

    public void sendCancelled(List<PlayerNotification> notifications) {

        List<Long> ids = notifications.stream()
                .map(PlayerNotification::getId)
                .toList();

        Map<Long, Long> telegramMap = repo.findTelegramIdsByNotificationIds(ids)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        int success = 0;
        int failed = 0;

        for (PlayerNotification pn : notifications) {
            Long telegramId = telegramMap.get(pn.getId());
            if (telegramId == null) continue;

            try {
                notificationService.send(
                        telegramId,
                        cancelBuilder.build(pn)
                );
                success++;
            } catch (Exception e) {
                failed++;
                log.error("❌ cancel send failed: telegramId={}", telegramId, e);
            }
        }

        log.info("📩 cancel notifications: success={}, failed={}", success, failed);
    }
}