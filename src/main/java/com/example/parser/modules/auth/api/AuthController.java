package com.example.parser.modules.auth.api;

import com.example.parser.modules.auth.api.dto.*;
import com.example.parser.modules.player.api.dto.MessageResponse;
import com.example.parser.modules.auth.mapping.PlayerDtoMapper;
import com.example.parser.modules.player.service.PlayerService;
import com.example.parser.modules.player.service.auth.PlayerAuthenticationService;
import com.example.parser.modules.player.service.auth.PlayerPasswordResetService;
import com.example.parser.modules.player.service.auth.PlayerRegistrationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping(AuthApi.BASE_PATH)
@RequiredArgsConstructor
public class AuthController {

    private final PlayerRegistrationService registrationService;
    private final PlayerAuthenticationService authenticationService;
    private final PlayerPasswordResetService passwordResetService;
    private final PlayerService playerService;
    private final PlayerDtoMapper mapper;

    @PostMapping(AuthApi.REGISTER)
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request, HttpSession session) {
        var pending = registrationService.initiate(request.getName(), request.getEmail(), request.getPassword());
        session.setAttribute("pending", pending);
        session.setMaxInactiveInterval(600);
        return ResponseEntity.ok(new MessageResponse(AuthApi.OK));
    }

    @PostMapping(AuthApi.VERIFY_EMAIL)
    public ResponseEntity<AuthResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request, HttpSession session) {
        var pending = (PlayerRegistrationService.Pending) session.getAttribute("pending");
        if (pending == null) return ResponseEntity.status(400).build();
        var player = registrationService.complete(pending, request.getCode());
        session.removeAttribute("pending");
        return ResponseEntity.ok(mapper.toAuthResponse(player));
    }

    @PostMapping(AuthApi.LOGIN)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        var player = authenticationService.authenticate(request.getEmail(), request.getPassword());
        var response = mapper.toAuthResponse(player);
        session.setAttribute(AuthApi.SESSION_ID, response.getId());
        session.setAttribute(AuthApi.SESSION_NAME, response.getName());
        session.setAttribute(AuthApi.SESSION_EMAIL, response.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping(AuthApi.ME)
    public ResponseEntity<MeResponse> me(HttpSession session) {
        var user = getSessionUser(session);
        if (user == null) return ResponseEntity.status(401).build();

        LocalDateTime createdAt = null;
        if (user.id() != null) {
            var player = playerService.findById(UUID.fromString(user.id()));
            if (player != null) createdAt = player.getCreatedAt();
        }

        return ResponseEntity.ok(new MeResponse(user.id(), user.name(), user.email(), createdAt));
    }

    @PostMapping(AuthApi.VERIFY_PASSWORD)
    public ResponseEntity<MessageResponse> verifyPassword(@Valid @RequestBody VerifyPasswordRequest request, HttpSession session) {
        var user = getSessionUser(session);
        if (user == null || user.id() == null) return ResponseEntity.status(401).build();
        playerService.verifyPassword(UUID.fromString(user.id()), request.getPassword());
        return ResponseEntity.ok(new MessageResponse(AuthApi.OK));
    }

    @PostMapping(AuthApi.LOGOUT)
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @PostMapping(AuthApi.FORGOT_PASSWORD)
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, HttpSession session) {
        var pending = passwordResetService.initiate(request.getEmail());
        session.setAttribute("reset", pending);
        session.setMaxInactiveInterval(600);
        return ResponseEntity.ok(new MessageResponse(AuthApi.OK));
    }

    @PostMapping(AuthApi.RESET_PASSWORD)
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request, HttpSession session) {
        var pending = (PlayerPasswordResetService.Pending) session.getAttribute("reset");
        if (pending == null) return ResponseEntity.status(400).body(new MessageResponse(AuthApi.CODE_EXPIRED));
        passwordResetService.complete(pending.email(), request.getCode(), pending.code(), request.getPassword());
        session.removeAttribute("reset");
        return ResponseEntity.ok(new MessageResponse(AuthApi.OK));
    }

    private SessionUser getSessionUser(HttpSession session) {
        String name = (String) session.getAttribute(AuthApi.SESSION_NAME);
        if (name == null) return null;
        return new SessionUser(
                (String) session.getAttribute(AuthApi.SESSION_ID),
                name,
                (String) session.getAttribute(AuthApi.SESSION_EMAIL)
        );
    }

    private record SessionUser(String id, String name, String email) {}
}