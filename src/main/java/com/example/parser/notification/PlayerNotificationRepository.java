package com.example.parser.notification;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlayerNotificationRepository
        extends JpaRepository<PlayerNotification, Long> {

    // 🔥 было: existsByTelegramIdAndTournamentId
    boolean existsByPlayerAndTournamentId(Player player, Long tournamentId);

    List<PlayerNotification> findByStartedFalse();

    List<PlayerNotification> findByFinishedFalse();

    void deleteByFinishedTrueAndDateBefore(LocalDate date);

    List<PlayerNotification> findByReminderSentFalse();

    List<PlayerNotification> findByDateOrderByTimeAsc(LocalDate date);
}