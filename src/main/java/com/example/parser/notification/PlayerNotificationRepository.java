package com.example.parser.notification;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlayerNotificationRepository
        extends JpaRepository<PlayerNotification, Long> {

    boolean existsByPlayerAndTournament_ExternalId(Player player, Long externalId);

    List<PlayerNotification> findByTournament_StartedFalse();

    List<PlayerNotification> findByTournament_FinishedFalse();

    void deleteByTournament_FinishedTrueAndTournament_DateBefore(LocalDate date);

    List<PlayerNotification> findByReminderSentFalse();

    List<PlayerNotification> findByTournament_DateOrderByTournament_TimeAsc(LocalDate date);

    List<PlayerNotification> findByTournament_Date(LocalDate date);

    @Query("""
        SELECT pn.id, p.telegramId
        FROM PlayerNotification pn
        JOIN pn.player p
        WHERE pn.id IN :ids
    """)
    List<Object[]> findTelegramIdsByNotificationIds(List<Long> ids);

    @Query("""
    SELECT pn
    FROM PlayerNotification pn
    JOIN FETCH pn.tournament t
    WHERE t.started = false
""")
    List<PlayerNotification> findPendingWithTournament();
}