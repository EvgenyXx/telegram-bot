package com.example.parser.modules.player.service;

import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.domain.Subscription;
import com.example.parser.modules.player.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlayerService playerService;

    @Transactional
    public void activate(UUID playerId, int days) {
        Player player = playerService.getById(playerId);

        Subscription subscription = player.getSubscription();
        if (subscription == null) {
            subscription = Subscription.builder()
                    .player(player)
                    .build();
        }
        subscription.activate(days);
        subscriptionRepository.save(subscription);

        log.info("✅ Подписка активирована для {} на {} дней", player.getEmail(), days);
    }

    public Subscription getByPlayerId(UUID playerId) {
        Player player = playerService.getById(playerId);
        return player.getSubscription();
    }

    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(UUID playerId) {
        var sub = subscriptionRepository.findByPlayerId(playerId);
        log.info("🔥 SUB RAW: {}", sub.isPresent() ? sub.get().isActiveNow() : "null");
        return sub.map(Subscription::isActiveNow).orElse(false);
    }
}