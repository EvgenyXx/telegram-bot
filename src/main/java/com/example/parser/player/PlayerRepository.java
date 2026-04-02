package com.example.parser.player;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player,Long> {
    Optional<Player> findByTelegramId(Long telegramId);
    Optional<Player> findByNameIgnoreCase(String name);

    Optional<Player> findByName(String name);

    Page<Player> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
