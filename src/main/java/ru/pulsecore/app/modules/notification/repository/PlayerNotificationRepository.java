package ru.pulsecore.app.modules.notification.repository;

import ru.pulsecore.app.modules.notification.domain.PlayerNotification;
import ru.pulsecore.app.modules.player.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerNotificationRepository
        extends JpaRepository<PlayerNotification, Long> {

    boolean existsByPlayerAndTournament_ExternalId(Player player, Long externalId);

    void deleteByTournament_FinishedTrueAndTournament_DateBefore(LocalDate date);

    List<PlayerNotification> findByTournament_Date(LocalDate date);

    @Query("""
        SELECT pn.id, p.id
        FROM PlayerNotification pn
        JOIN pn.player p
        WHERE pn.id IN :ids
    """)
    List<Object[]> findIdsByNotificationIds(List<Long> ids);

    @Query("""
        SELECT pn
        FROM PlayerNotification pn
        JOIN FETCH pn.tournament t
        WHERE t.started = false
    """)
    List<PlayerNotification> findPendingWithTournament();

    @Query("""
        SELECT pn
        FROM PlayerNotification pn
        JOIN FETCH pn.player
        JOIN FETCH pn.tournament t
        WHERE t.finished = false
    """)
    List<PlayerNotification> findNotFinishedFull();

    @Query("""
    SELECT pn
    FROM PlayerNotification pn
    JOIN FETCH pn.tournament t
    WHERE t.date = :date
""")
    List<PlayerNotification> findTodayWithTournament(LocalDate date);

    @Query("""
    SELECT pn FROM PlayerNotification pn
    JOIN FETCH pn.tournament t
    WHERE pn.player = :player AND t.date >= CURRENT_DATE
    ORDER BY t.date ASC, t.time ASC
    LIMIT 1
""")
    Optional<PlayerNotification> findNextTournament(Player player);
}