package com.example.parser.modules.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;



    @PostMapping("/telegram")
    public ResponseEntity<String> telegramAuth(@RequestBody TelegramAuthRequest request) {
        String token = authService.authenticate(request);
        return ResponseEntity.ok(token);
    }
}