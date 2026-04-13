package com.example.parser.notification.start;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.notification.NotificationService;
import com.example.parser.notification.PlayerNotificationRepository;
import com.example.parser.notification.formatter.TournamentCancelledMessageBuilder;
import com.example.parser.notification.formatter.TournamentStartMessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentNotificationService {

    private final PlayerNotificationRepository repo;
    private final NotificationService notificationService;
    private final TournamentStartMessageBuilder startBuilder;
    private final TournamentCancelledMessageBuilder cancelBuilder;

    public int sendStart(List<PlayerNotification> notifications) {
        var telegramMap = getTelegramMap(notifications);

        int success = 0;

        for (PlayerNotification pn : notifications) {
            Long telegramId = telegramMap.get(pn.getId());
            if (telegramId == null) continue;

            try {
                notificationService.send(telegramId, startBuilder.build(pn));
                success++;
            } catch (Exception e) {
                log.error("❌ start send failed: telegramId={}", telegramId, e);
            }
        }

        return success;
    }

    public void sendCancelled(List<PlayerNotification> notifications) {
        var telegramMap = getTelegramMap(notifications);

        for (PlayerNotification pn : notifications) {
            Long telegramId = telegramMap.get(pn.getId());
            if (telegramId == null) continue;

            try {
                notificationService.send(telegramId, cancelBuilder.build(pn));
            } catch (Exception e) {
                log.error("❌ cancel send failed: telegramId={}", telegramId, e);
            }
        }
    }

    private Map<Long, Long> getTelegramMap(List<PlayerNotification> notifications) {
        List<Long> ids = notifications.stream()
                .map(PlayerNotification::getId)
                .toList();

        return repo.findTelegramIdsByNotificationIds(ids)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }
}