package com.example.parser.repository;

import com.example.parser.entity.Player;
import com.example.parser.entity.TournamentResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
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
}