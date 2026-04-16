package com.example.parser.modules.player.repository;


import com.example.parser.modules.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player,Long> {
    Optional<Player> findByTelegramId(Long telegramId);




    Page<Player> findByNameContainingIgnoreCase(String name, Pageable pageable);

    boolean existsByNameIgnoreCase(String name);
}
