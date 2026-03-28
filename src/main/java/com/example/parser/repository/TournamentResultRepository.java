package com.example.parser.repository;

import com.example.parser.entity.Player;
import com.example.parser.entity.TournamentResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TournamentResultRepository extends JpaRepository<TournamentResultEntity, Long> {

    List<TournamentResultEntity> findByPlayerAndDateBetweenOrderByDateAsc(
            Player player,
            LocalDate start,
            LocalDate end
    );

    @Query("""
    SELECT COALESCE(SUM(t.amount), 0)
    FROM TournamentResultEntity t
    WHERE t.player = :player
      AND t.date BETWEEN :start AND :end
""")
    int sumByPlayerAndPeriod(Player player, LocalDate start, LocalDate end);
}