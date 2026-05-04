package com.example.parser.modules.payment;

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
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String API_URL = "https://api.yookassa.ru/v3/payments";
    private static final String RETURN_URL = "https://pulsecore-app.ru/dashboard.html";
    private static final String CURRENCY = "RUB";
    private static final Map<Integer, Integer> PRICES = Map.of(1, 100, 2, 200);

    public record PaymentResponse(String confirmationUrl, String paymentId) {}

    public PaymentResponse createPayment(UUID playerId, int months) {
        validateMonths(months);

        HttpEntity<Map<String, Object>> request = buildRequest(playerId, months);
        ResponseEntity<Map<String, Object>> response = executeRequest(request);
        return extractPaymentResponse(response);
    }

    private void validateMonths(int months) {
        if (!PRICES.containsKey(months)) {
            throw new PaymentException("Неверный срок подписки: " + months);
        }
    }

    private HttpEntity<Map<String, Object>> buildRequest(UUID playerId, int months) {
        int amount = PRICES.get(months);

        Map<String, Object> body = Map.of(
                "amount", Map.of("value", String.valueOf(amount), "currency", CURRENCY),
                "confirmation", Map.of("type", "redirect", "return_url", RETURN_URL),
                "description", "Подписка PulseCore на " + months + " мес.",
                "metadata", Map.of("playerId", playerId.toString(), "months", String.valueOf(months)),
                "capture", true
        );

        return new HttpEntity<>(body, createHeaders());
    }

    private HttpHeaders createHeaders() {
        String auth = props.getShopId() + ":" + props.getSecretKey();
        String encoded = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Idempotence-Key", UUID.randomUUID().toString());
        headers.set("Authorization", "Basic " + encoded);
        return headers;
    }

    private ResponseEntity<Map<String, Object>> executeRequest(HttpEntity<Map<String, Object>> request) {
        try {
            return restTemplate.exchange(
                    API_URL, HttpMethod.POST, request,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);
        } catch (Exception e) {
            log.error("YooKassa API request failed", e);
            throw new PaymentException("Не удалось создать платёж");
        }
    }

    private PaymentResponse extractPaymentResponse(ResponseEntity<Map<String, Object>> response) {
        Map<String, Object> body = response.getBody();
        String paymentId = (String) body.get("id");
        Map<String, Object> confirmation = (Map<String, Object>) body.get("confirmation");
        String url = (String) confirmation.get("confirmation_url");

        log.info("Payment created: id={}, url={}", paymentId, url);
        return new PaymentResponse(url, paymentId);
    }
}