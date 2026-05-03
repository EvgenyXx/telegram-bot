package com.example.parser.modules.player.api;

import com.example.parser.modules.auth.api.dto.ChangePasswordRequest;
import com.example.parser.modules.auth.api.dto.UpdateProfileRequest;
import com.example.parser.modules.payment.YookassaService;
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
    private final YookassaService yookassaService;

    @GetMapping(PlayerApi.DASHBOARD)
    public ResponseEntity<DashboardResponse> getDashboard(@PathVariable UUID id) {
        return ResponseEntity.ok(playerStatsService.getDashboard(id));
    }

    @GetMapping(PlayerApi.SUM)
    public ResponseEntity<SumResponse> getSumById(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(playerStatsService.getSum(id, start, end));
    }

    @GetMapping(PlayerApi.TOURNAMENTS)
    public ResponseEntity<TournamentListResponse> getTournamentsById(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(playerStatsService.getTournaments(id, start, end));
    }

    @PutMapping(PlayerApi.PROFILE)
    public ResponseEntity<PlayerProfileResponse> updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(playerService.updateProfile(id, request));
    }

    @PutMapping(PlayerApi.CHANGE_PASSWORD)
    public ResponseEntity<MessageResponse> changePassword(
            @PathVariable UUID id,
            @Valid @RequestBody ChangePasswordRequest request) {
        playerService.changePassword(id, request);
        return ResponseEntity.ok(new MessageResponse("Пароль изменён"));
    }

    @PostMapping(PlayerApi.SUBSCRIBE)
    public ResponseEntity<MessageResponse> subscribe(@PathVariable UUID id, @RequestParam(defaultValue = "30") int days) {
        subscriptionService.activate(id, days);
        return ResponseEntity.ok(new MessageResponse("Подписка активирована на " + days + " дней"));
    }

    @GetMapping(PlayerApi.SEARCH)
    public ResponseEntity<List<PlayerResponse>> search(@RequestParam(PlayerApi.SEARCH_PARAM) String q) {
        return ResponseEntity.ok(playerService.searchPlayers(q));
    }

    @GetMapping(PlayerApi.SUBSCRIPTION)
    public ResponseEntity<SubscriptionStatusResponse> getSubscription(@PathVariable UUID id) {
        Subscription sub = subscriptionService.getByPlayerId(id);
        if (sub == null) return ResponseEntity.ok(new SubscriptionStatusResponse(false, null, null));
        return ResponseEntity.ok(SubscriptionStatusResponse.builder()
                .active(sub.isActiveNow())
                .expiresAt(sub.getExpiresAt() != null ? sub.getExpiresAt().toString() : null)
                .startedAt(sub.getStartedAt() != null ? sub.getStartedAt().toString() : null)
                .build());
    }

    @DeleteMapping(PlayerApi.DELETE_ACCOUNT)
    public ResponseEntity<MessageResponse> deleteAccount(@PathVariable UUID id) {
        playerService.deletePlayer(id);
        return ResponseEntity.ok(new MessageResponse("Аккаунт удалён"));
    }

    @PostMapping(PlayerApi.PAY)
    public ResponseEntity<PaymentResponse> pay(@PathVariable UUID id, @RequestParam int months) {
        var payment = yookassaService.createPayment(id, months);
        return ResponseEntity.ok(new PaymentResponse(payment.confirmationUrl()));
    }
}