package ru.pulsecore.app.modules.lineup.repository;

import ru.pulsecore.app.modules.lineup.domain.Lineup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LineupRepository extends JpaRepository<Lineup, Long> {

    Optional<Lineup> findByLeagueAndTimeAndDate(String league, String time, LocalDate date);

    List<Lineup> findByDate(LocalDate date);

    List<Lineup> findByDateBetweenOrderByDateAscTimeAsc(LocalDate start, LocalDate end);


    void deleteByDateBefore(LocalDate yesterday);
}