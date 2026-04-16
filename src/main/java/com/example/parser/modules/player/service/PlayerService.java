package com.example.parser.modules.player.service;

import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    // ✅ регистрация (идемпотентная)
    public Player registerIfNotExists(Long telegramId, String name) {
        return playerRepository.findByTelegramId(telegramId)
                .orElseGet(() -> playerRepository.save(
                        Player.builder()
                                .telegramId(telegramId)
                                .name(name)
                                .createdAt(LocalDateTime.now())
                                .build()
                ));
    }

    // ✅ получение (без exception)
    public Player getByTelegramId(Long telegramId) {
        return playerRepository.findByTelegramId(telegramId).orElse(null);
    }

    public List<Player> getAll() {
        return playerRepository.findAll();
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

    public Page<Player> search(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return playerRepository.findByNameContainingIgnoreCase(query, pageable);
    }

    // 🔥 пока заглушка — ок оставить
    public void updateSum(Long playerId, Long sum) {
        // TODO later
    }
}