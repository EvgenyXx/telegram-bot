package com.example.parser.repository;

import com.example.parser.domain.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player,Long> {
    Optional<Player> findByTelegramId(Long telegramId);
    Optional<Player> findByNameIgnoreCase(String name);

    Optional<Player> findByName(String name);
}
