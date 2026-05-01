package com.example.parser.modules.auth;

import com.example.parser.modules.auth.api.AuthApi;
import com.example.parser.modules.auth.dto.*;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.service.PlayerAuthService;
import com.example.parser.modules.player.service.PlayerService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(AuthApi.BASE_PATH)
@RequiredArgsConstructor
public class AuthController {

    private final PlayerAuthService playerAuthService;
    private final PlayerService playerService;

    @PostMapping(AuthApi.REGISTER)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, HttpSession session) {
        PlayerAuthService.PendingRegistration pending = playerAuthService.initiateRegistration(
                request.getName(), request.getEmail(), request.getPassword());

        session.setAttribute("pending", pending);
        session.setMaxInactiveInterval(600);

        return ResponseEntity.ok(Map.of(AuthApi.RESP_MESSAGE, AuthApi.OK));
    }

    @PostMapping(AuthApi.VERIFY_EMAIL)
    public ResponseEntity<AuthResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request, HttpSession session) {
        PlayerAuthService.PendingRegistration pending = (PlayerAuthService.PendingRegistration) session.getAttribute("pending");

        if (pending == null) {
            return ResponseEntity.status(400).build();
        }

        AuthResponse response = playerAuthService.completeRegistration(pending, request.getCode());

        session.removeAttribute("pending");

        return ResponseEntity.ok(response);
    }

    @PostMapping(AuthApi.LOGIN)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        AuthResponse response = playerAuthService.authenticate(request.getEmail(), request.getPassword());
        session.setAttribute(AuthApi.SESSION_ID, response.getId());
        session.setAttribute(AuthApi.SESSION_NAME, response.getName());
        session.setAttribute(AuthApi.SESSION_EMAIL, response.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping(AuthApi.ME)
    public ResponseEntity<?> me(HttpSession session) {
        String id = (String) session.getAttribute(AuthApi.SESSION_ID);
        String name = (String) session.getAttribute(AuthApi.SESSION_NAME);
        String email = (String) session.getAttribute(AuthApi.SESSION_EMAIL);
        if (name == null) return ResponseEntity.status(401).build();
        if (id != null) {
            Player player = playerService.findById(UUID.fromString(id));
            return ResponseEntity.ok(Map.of(
                    AuthApi.RESP_ID, id,
                    AuthApi.RESP_NAME, name,
                    AuthApi.RESP_EMAIL, email != null ? email : "",
                    AuthApi.RESP_CREATED_AT, player != null ? player.getCreatedAt() : null
            ));
        }
        return ResponseEntity.ok(Map.of(AuthApi.RESP_NAME, name, AuthApi.RESP_EMAIL, email != null ? email : ""));
    }

    @PostMapping(AuthApi.VERIFY_PASSWORD)
    public ResponseEntity<?> verifyPassword(@Valid @RequestBody VerifyPasswordRequest request, HttpSession session) {
        String id = (String) session.getAttribute(AuthApi.SESSION_ID);
        if (id == null) return ResponseEntity.status(401).build();
        playerService.verifyPassword(UUID.fromString(id), request.getPassword());
        return ResponseEntity.ok(Map.of(AuthApi.RESP_MESSAGE, AuthApi.OK));
    }

    @PostMapping(AuthApi.LOGOUT)
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @PostMapping(AuthApi.FORGOT_PASSWORD)
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpSession session) {
        PlayerAuthService.PendingReset reset = playerAuthService.initiatePasswordReset(request.getEmail());

        session.setAttribute("reset", reset);
        session.setMaxInactiveInterval(600);

        return ResponseEntity.ok(Map.of(AuthApi.RESP_MESSAGE, AuthApi.OK));
    }

    @PostMapping(AuthApi.RESET_PASSWORD)
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpSession session) {
        PlayerAuthService.PendingReset reset = (PlayerAuthService.PendingReset) session.getAttribute("reset");

        if (reset == null) {
            return ResponseEntity.status(400).body(Map.of(AuthApi.RESP_MESSAGE, AuthApi.CODE_EXPIRED));
        }

        playerAuthService.completePasswordReset(reset.email(), request.getCode(), reset.code(), request.getPassword());

        session.removeAttribute("reset");

        return ResponseEntity.ok(Map.of(AuthApi.RESP_MESSAGE, AuthApi.OK));
    }
}