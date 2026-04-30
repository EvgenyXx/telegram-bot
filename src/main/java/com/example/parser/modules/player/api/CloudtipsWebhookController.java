package com.example.parser.modules.player.api;

import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.repository.PlayerRepository;
import com.example.parser.modules.player.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CloudtipsWebhookController {

    private final PlayerRepository playerRepository;
    private final SubscriptionService subscriptionService;

    @PostMapping("/api/payment/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody Map<String, Object> payload) {
        log.info("💰 Webhook received: {}", payload);

        try {
            // CloudTips присылает имя плательщика в поле "name"
            String payerName = (String) payload.getOrDefault("name", "");
            double amount = Double.parseDouble(payload.getOrDefault("amount", "0").toString());

            // Ищем игрока по имени (имя в донате должно совпадать с именем в PulseCore)
            Player player = playerRepository.findByNameIgnoreCase(payerName).orElse(null);

            if (player == null) {
                log.warn("⚠️ Игрок не найден по имени: {}", payerName);
                return ResponseEntity.ok(Map.of("status", "ignored", "reason", "player not found"));
            }

            int days = amount >= 800 ? 60 : 30;
            subscriptionService.activate(player.getId(), days);

            log.info("✅ Подписка активирована для {} ({} ₽, {} дней)", player.getEmail(), amount, days);
            return ResponseEntity.ok(Map.of("status", "ok", "player", player.getEmail(), "days", days));

        } catch (Exception e) {
            log.error("❌ Ошибка обработки вебхука", e);
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}