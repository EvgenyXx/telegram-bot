package ru.pulsecore.app.modules.player.scheduler;

import ru.pulsecore.app.modules.player.domain.Player;
import ru.pulsecore.app.modules.player.repository.PlayerRepository;
import ru.pulsecore.app.modules.player.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnverifiedCleanupScheduler {

    private final PlayerRepository playerRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanUnverified() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<Player> unverified = playerRepository.findByVerifiedFalseAndCreatedAtBefore(cutoff);

        for (Player p : unverified) {
            subscriptionRepository.deleteByPlayer(p);
        }
        playerRepository.deleteAll(unverified);

        log.info("Удалено неподтверждённых пользователей: {}", unverified.size());
    }
}