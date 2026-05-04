package ru.pulsecore.app.modules.notification.start;

import ru.pulsecore.app.modules.notification.domain.PlayerNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentNotificationService {

    public int sendStart(List<PlayerNotification> notifications) {
        for (PlayerNotification pn : notifications) {
            log.info("🚀 Tournament start: player={}, tournament={}",
                    pn.getPlayer().getId(), pn.getTournament().getId());
        }
        log.info("📩 Start notifications: {}", notifications.size());
        return notifications.size();
    }

    public void sendCancelled(List<PlayerNotification> notifications) {
        for (PlayerNotification pn : notifications) {
            log.info("❌ Tournament cancelled: player={}, tournament={}",
                    pn.getPlayer().getId(), pn.getTournament().getId());
        }
        log.info("📩 Cancelled notifications: {}", notifications.size());
    }
}