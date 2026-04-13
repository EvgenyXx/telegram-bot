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

    void deleteByTournament_FinishedTrueAndTournament_DateBefore(LocalDate date);

    List<PlayerNotification> findByTournament_Date(LocalDate date);

    @Query("""
        SELECT pn.id, p.telegramId
        FROM PlayerNotification pn
        JOIN pn.player p
        WHERE pn.id IN :ids
    """)
    List<Object[]> findTelegramIdsByNotificationIds(List<Long> ids);

    // 🔥 старт
    @Query("""
        SELECT pn
        FROM PlayerNotification pn
        JOIN FETCH pn.tournament t
        WHERE t.started = false
    """)
    List<PlayerNotification> findPendingWithTournament();

    // 🔥 финиш (с player чтобы не было Lazy)
    @Query("""
        SELECT pn
        FROM PlayerNotification pn
        JOIN FETCH pn.player
        JOIN FETCH pn.tournament t
        WHERE t.finished = false
    """)
    List<PlayerNotification> findNotFinishedFull();

}