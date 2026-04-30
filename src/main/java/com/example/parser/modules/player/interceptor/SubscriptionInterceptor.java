package com.example.parser.modules.player.interceptor;

import com.example.parser.modules.player.service.SubscriptionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionInterceptor implements HandlerInterceptor {

    private final SubscriptionService subscriptionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String playerId = (String) request.getSession().getAttribute("playerId");
        if (playerId == null) {
            response.setStatus(401);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"message\":\"Не авторизован\"}");
            return false;
        }

        boolean active = subscriptionService.hasActiveSubscription(UUID.fromString(playerId));
        log.info("🔥 SUBSCRIPTION CHECK: {} -> {}", playerId, active);

        if (!active) {
            response.setStatus(402);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"message\":\"Требуется активная подписка\"}");
            return false;
        }
        return true;
    }
}