package com.example.parser.service;

import com.example.parser.dto.FullStatsDto;
import com.example.parser.dto.FullStatsProjection;
import com.example.parser.dto.PeriodStatsProjection;
import com.example.parser.entity.Player;
import com.example.parser.entity.TournamentResultEntity;
import com.example.parser.repository.TournamentResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentResultService {

    private final TournamentResultRepository repository;

    public void save(TournamentResultEntity entity) {

        if (repository.existsByPlayerAndTournamentId(
                entity.getPlayer(),
                entity.getTournamentId()
        )) {
            return;
        }

        try {
            repository.save(entity);
        } catch (Exception ignored) {}
    }

    public List<TournamentResultEntity> getResultsByPeriod(Player player,
                                                           LocalDate start,
                                                           LocalDate end) {
        return repository.findByPlayerAndDateBetweenOrderByDateAsc(player, start, end);
    }



    public PeriodStatsProjection getStatsByPeriod(Player player, LocalDate start, LocalDate end) {
        return repository.getStats(player, start, end);
    }

    public boolean exists(Long playerId, Long tournamentId) {
        return repository.existsByPlayerIdAndTournamentId(playerId, tournamentId);
    }

    public FullStatsDto getFullStats(Player player) {

        FullStatsProjection stats = repository.getFullStats(player);

        if (stats == null || stats.getCount() == 0) {
            return null;
        }

        long count = stats.getCount();
        double sum = stats.getSum() != null ? stats.getSum() : 0;
        double avg = stats.getAvg() != null ? stats.getAvg() : 0;

        return new FullStatsDto(count, sum, avg);
    }
}