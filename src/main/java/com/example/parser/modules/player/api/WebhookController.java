package com.example.parser.modules.player.api;

import com.example.parser.modules.player.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebhookController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/api/payment/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> body) {
        log.info("Webhook received: {}", body);

        try {
            String event = (String) body.get("event");
            if (!"payment.succeeded".equals(event)) {
                return ResponseEntity.ok("ignored");
            }

            Map<String, Object> payment = (Map<String, Object>) body.get("object");
            Map<String, Object> metadata = (Map<String, Object>) payment.get("metadata");

            if (metadata == null || !metadata.containsKey("playerId")) {
                log.warn("No playerId in metadata");
                return ResponseEntity.ok("ignored");
            }

            UUID playerId = UUID.fromString((String) metadata.get("playerId"));
            int months = Integer.parseInt((String) metadata.get("months"));

            subscriptionService.activate(playerId, months * 30);
            log.info("Subscription activated: playerId={}, months={}", playerId, months);

            return ResponseEntity.ok("ok");

        } catch (Exception e) {
            log.error("Webhook error", e);
            return ResponseEntity.ok("error");
        }
    }
}