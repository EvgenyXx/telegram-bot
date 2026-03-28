package com.example.parser.service;

import com.example.parser.entity.Player;
import com.example.parser.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
                            .createdAt(LocalDateTime.now())
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


    public List<Player> getAll() {
        return playerRepository.findAll();
    }

    public Player findByName(String name) {
        return playerRepository.findByName(name).orElse(null);
    }

    public Player findById(Long id) {
        return playerRepository.findById(id).orElse(null);
    }
}