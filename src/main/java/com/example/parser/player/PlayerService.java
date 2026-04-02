package com.example.parser.player;

import com.example.parser.domain.entity.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    // ✅ РЕГИСТРАЦИЯ ТОЛЬКО 1 РАЗ
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

    // ✅ НЕ КИДАЕМ ОШИБКУ
    public Player getByTelegramId(Long telegramId) {
        return playerRepository.findByTelegramId(telegramId).orElse(null);
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

    public void block(Long playerId) {
        Player player = playerRepository.findById(playerId).orElseThrow();
        player.setBlocked(true);
        playerRepository.save(player);
    }

    public void unblock(Long playerId) {
        Player player = playerRepository.findById(playerId).orElseThrow();
        player.setBlocked(false);
        playerRepository.save(player);
    }
}