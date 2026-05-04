package ru.pulsecore.app.modules.notification.repository;

import ru.pulsecore.app.modules.notification.domain.PlayerNotification;
import ru.pulsecore.app.modules.player.domain.Player;
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


}