package com.example.parser.modules.auth;

import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PlayerRepository playerRepository;



    public String authenticate(TelegramAuthRequest request) {

        // 1. базовая проверка
        if (request.id == null) {
            throw new RuntimeException("Invalid telegram data");
        }

        // 2. ищем пользователя
        Player player = playerRepository
                .findByTelegramId(request.id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. пока просто возвращаем ID (потом сделаем JWT)
        return "USER_ID_" + player.getId();
    }
}