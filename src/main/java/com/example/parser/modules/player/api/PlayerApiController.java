package com.example.parser.modules.player.api;

import com.example.parser.modules.auth.dto.ChangePasswordRequest;
import com.example.parser.modules.auth.dto.UpdateProfileRequest;
import com.example.parser.modules.player.api.dto.*;
import com.example.parser.modules.player.domain.Subscription;
import com.example.parser.modules.player.service.PlayerService;
import com.example.parser.modules.player.service.PlayerStatsService;
import com.example.parser.modules.player.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(PlayerApi.BASE_PATH)
@RequiredArgsConstructor
public class PlayerApiController {

    private final PlayerService playerService;
    private final PlayerStatsService playerStatsService;
    private final SubscriptionService subscriptionService;

    @GetMapping(PlayerApi.DASHBOARD)
    public ResponseEntity<DashboardResponse> getDashboard(@PathVariable UUID id) {
        log.info("📊 Dashboard: {}", id);
        return ResponseEntity.ok(playerStatsService.getDashboard(id));
    }

    @GetMapping(PlayerApi.SUM)
    public ResponseEntity<SumResponse> getSumById(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        log.info("💰 Сумма: {} с {} по {}", id, start, end);
        return ResponseEntity.ok(playerStatsService.getSum(id, start, end));
    }

    @GetMapping(PlayerApi.TOURNAMENTS)
    public ResponseEntity<TournamentListResponse> getTournamentsById(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        log.info("📋 Турниры: {} с {} по {}", id, start, end);
        return ResponseEntity.ok(playerStatsService.getTournaments(id, start, end));
    }

    @PutMapping(PlayerApi.PROFILE)
    public ResponseEntity<PlayerProfileResponse> updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("👤 Обновление профиля: {}", id);
        return ResponseEntity.ok(playerService.updateProfile(id, request));
    }

    @PutMapping(PlayerApi.CHANGE_PASSWORD)
    public ResponseEntity<?> changePassword(
            @PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("🔐 Смена пароля: {}", id);
        playerService.changePassword(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping(PlayerApi.SUBSCRIBE)
    public ResponseEntity<?> subscribe(@PathVariable UUID id, @RequestParam(defaultValue = "30") int days) {
        log.info("💳 Активация подписки: {} на {} дней", id, days);
        subscriptionService.activate(id, days);
        return ResponseEntity.ok(Map.of("message", "Подписка активирована на " + days + " дней"));
    }

    @GetMapping(PlayerApi.SEARCH)
    public ResponseEntity<List<PlayerResponse>> search(@RequestParam(PlayerApi.SEARCH_PARAM) String q) {
        log.info("🔍 Поиск игроков: {}", q);
        return ResponseEntity.ok(playerService.searchPlayers(q));
    }

    @GetMapping(PlayerApi.SUBSCRIPTION)
    public ResponseEntity<?> getSubscription(@PathVariable UUID id) {
        log.info("📋 Проверка подписки: {}", id);
        Subscription sub = subscriptionService.getByPlayerId(id);
        if (sub == null) {
            return ResponseEntity.ok(Map.of("active", false));
        }
        return ResponseEntity.ok(Map.of(
                "active", sub.isActiveNow(),
                "expiresAt", sub.getExpiresAt() != null ? sub.getExpiresAt().toString() : null,
                "startedAt", sub.getStartedAt() != null ? sub.getStartedAt().toString() : null
        ));
    }


}