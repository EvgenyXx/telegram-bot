package ru.pulsecore.app.modules.tournament.persistence.repository;


import org.springframework.stereotype.Repository;
import ru.pulsecore.app.core.dto.PeriodStatsProjection;
import ru.pulsecore.app.modules.player.domain.Player;

import ru.pulsecore.app.modules.tournament.persistence.entity.TournamentResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentResultRepository extends JpaRepository<TournamentResultEntity, Long> {



    boolean existsByPlayerAndTournament_ExternalId(Player player, Long externalId);


    List<TournamentResultEntity> findByPlayerAndDateBetweenOrderByDateAsc(
            Player player,
            LocalDate start,
            LocalDate end
    );




    @Query("""
    SELECT\s
        COUNT(t) as count,
        COALESCE(SUM(t.amount), 0) as sum,
        COALESCE(AVG(t.amount), 0) as average,
        COALESCE(SUM(t.amount) * 0.97, 0) as minusThreePercent
    FROM TournamentResultEntity t
    WHERE t.player = :player
    AND t.date BETWEEN :start AND :end
""")
    PeriodStatsProjection getStats(Player player, LocalDate start, LocalDate end);







    Optional<TournamentResultEntity> findTopByPlayerOrderByDateDesc(Player player);


    Optional<TournamentResultEntity> findByPlayerAndTournament_ExternalId(Player player, Long externalId);
}