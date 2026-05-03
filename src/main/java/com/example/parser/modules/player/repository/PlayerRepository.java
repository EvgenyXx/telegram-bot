package com.example.parser.modules.player.repository;

import com.example.parser.modules.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {

    Optional<Player> findByAccessCode(String accessCode);

    boolean existsByEmail(String email);

    boolean existsByNameIgnoreCase(String name);

    Optional<Player> findByEmail(String email);

    Optional<Player> findByNameIgnoreCase(String name);

    List<Player> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM Player p WHERE p.verified = false AND p.createdAt < :cutoff")
    int deleteUnverifiedOlderThan(@Param("cutoff") LocalDateTime cutoff);

    List<Player> findByVerifiedFalseAndCreatedAtBefore(LocalDateTime cutoff);
}