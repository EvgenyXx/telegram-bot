package com.example.parser.modules.payment;

import com.example.parser.modules.player.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class YookassaService {

    private final YookassaProperties props;
    private final SubscriptionService subscriptionService;
    private final RestTemplate restTemplate = new RestTemplate();

    public record PaymentResponse(String confirmationUrl, String paymentId) {}

    public PaymentResponse createPayment(UUID playerId, int months) {
        int amount = months == 1 ? 100 : 200;

        String auth = props.getShopId() + ":" + props.getSecretKey();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotence-Key", UUID.randomUUID().toString());
        headers.set("Authorization", "Basic " + encodedAuth);

        Map<String, Object> body = Map.of(
                "amount", Map.of("value", String.valueOf(amount), "currency", "RUB"),
                "confirmation", Map.of("type", "redirect", "return_url", "https://pulsecore-app.ru/dashboard.html"),
                "description", "Подписка PulseCore на " + months + " мес.",
                "metadata", Map.of("playerId", playerId.toString(), "months", String.valueOf(months)),
                "capture", true
        );

        try {
            ResponseEntity<Map<String, Object>> res = restTemplate.exchange(
                    "https://api.yookassa.ru/v3/payments",
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    (Class<Map<String, Object>>) (Class<?>) Map.class);

            Map<String, Object> result = res.getBody();
            String paymentId = (String) result.get("id");
            Map<String, Object> confirmation = (Map<String, Object>) result.get("confirmation");
            String confirmationUrl = (String) confirmation.get("confirmation_url");

            log.info("Payment created: paymentId={}, url={}", paymentId, confirmationUrl);
            return new PaymentResponse(confirmationUrl, paymentId);

        } catch (Exception e) {
            log.error("Payment creation error", e);
            throw new RuntimeException("Не удалось создать платёж");
        }
    }

}