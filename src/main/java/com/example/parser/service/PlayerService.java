package com.example.parser.service;

import com.example.parser.entity.Player;
import com.example.parser.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;



    public Player registerIfNotExists(Long telegramId, String name) {
        return playerRepository.findByTelegramId(telegramId)
                .orElseGet(() -> {
                    Player player = Player.builder()
                            .telegramId(telegramId)
                            .name(name)
                            .build();

                    return playerRepository.save(player);
                });
    }

    public Player getByTelegramId(Long telegramId) {
        return playerRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    public Player save(Player player) {
        return playerRepository.save(player);
    }


}