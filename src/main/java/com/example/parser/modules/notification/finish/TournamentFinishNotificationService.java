package com.example.parser.modules.notification.finish;

import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.notification.repository.PlayerNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentFinishNotificationService {

    private final PlayerNotificationRepository repo;

    public void sendCancelled(List<PlayerNotification> notifications) {
        for (PlayerNotification pn : notifications) {
            log.info("❌ Tournament cancelled: player={}, tournament={}",
                    pn.getPlayer().getId(), pn.getTournament().getId());
        }
        log.info("📩 Cancelled notifications: {}", notifications.size());
    }
}