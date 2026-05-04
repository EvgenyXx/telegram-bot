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

    private static final String WEBHOOK_PATH = "/api/payment/webhook";
    private static final String EVENT_SUCCEEDED = "payment.succeeded";
    private static final String KEY_EVENT = "event";
    private static final String KEY_OBJECT = "object";
    private static final String KEY_METADATA = "metadata";
    private static final String KEY_PLAYER_ID = "playerId";
    private static final String KEY_MONTHS = "months";
    private static final int DAYS_PER_MONTH = 30;

    private static final String RESPONSE_OK = "ok";
    private static final String RESPONSE_IGNORED = "ignored";
    private static final String RESPONSE_ERROR = "error";

    @PostMapping(WEBHOOK_PATH)
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> body) {
        log.info("Webhook received: {}", body);

        try {
            if (!EVENT_SUCCEEDED.equals(body.get(KEY_EVENT))) {
                return ResponseEntity.ok(RESPONSE_IGNORED);
            }

            Map<String, Object> payment = getNestedMap(body, KEY_OBJECT);
            Map<String, Object> metadata = getNestedMap(payment, KEY_METADATA);

            if (!metadata.containsKey(KEY_PLAYER_ID)) {
                log.warn("No playerId in metadata");
                return ResponseEntity.ok(RESPONSE_IGNORED);
            }

            UUID playerId = UUID.fromString((String) metadata.get(KEY_PLAYER_ID));
            int months = Integer.parseInt((String) metadata.get(KEY_MONTHS));

            subscriptionService.activate(playerId, months * DAYS_PER_MONTH);
            log.info("Subscription activated: playerId={}, months={}", playerId, months);

            return ResponseEntity.ok(RESPONSE_OK);

        } catch (Exception e) {
            log.error("Webhook error", e);
            return ResponseEntity.ok(RESPONSE_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getNestedMap(Map<String, Object> source, String key) {
        return (Map<String, Object>) source.get(key);
    }
}