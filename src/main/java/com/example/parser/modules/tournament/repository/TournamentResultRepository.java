package com.example.parser.modules.tournament.repository;

import com.example.parser.core.dto.FullStatsProjection;
import com.example.parser.core.dto.PeriodStatsProjection;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.tournament.domain.Tournament;
import com.example.parser.modules.tournament.domain.TournamentResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface TournamentResultRepository extends JpaRepository<TournamentResultEntity, Long> {

    // 🔥 НОВЫЕ МЕТОДЫ (ГЛАВНОЕ)
    Optional<TournamentResultEntity> findByPlayerAndTournament_ExternalId(Player player, Long externalId);

    boolean existsByPlayerAndTournament_ExternalId(Player player, Long externalId);

    // 📊 старые методы (оставляем)
    List<TournamentResultEntity> findByPlayerAndDateBetweenOrderByDateAsc(
            Player player,
            LocalDate start,
            LocalDate end
    );

    boolean existsByPlayerAndTournament(Player player, Tournament tournament);

    // 📊 статистика за период
    @Query("""
    SELECT 
        COUNT(t) as count,
        COALESCE(SUM(t.amount), 0) as sum,
        COALESCE(AVG(t.amount), 0) as average,
        COALESCE(SUM(t.amount) * 0.97, 0) as minusThreePercent
    FROM TournamentResultEntity t
    WHERE t.player = :player
    AND t.date BETWEEN :start AND :end
""")
    PeriodStatsProjection getStats(Player player, LocalDate start, LocalDate end);

    // 📊 общая статистика
    @Query("""
        SELECT 
            COUNT(t) as count,
            SUM(t.amount) as sum,
            AVG(t.amount) as avg
        FROM TournamentResultEntity t
        WHERE t.player = :player
    """)
    FullStatsProjection getFullStats(Player player);

    @Query("""
SELECT r 
FROM TournamentResultEntity r
JOIN FETCH r.tournament
""")
    List<TournamentResultEntity> findAllWithTournament();
}