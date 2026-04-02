package com.example.parser.service;

import com.example.parser.domain.dto.FullStatsDto;
import com.example.parser.domain.dto.FullStatsProjection;
import com.example.parser.domain.dto.PeriodStatsProjection;
import com.example.parser.domain.dto.ResultDto;
import com.example.parser.domain.entity.Player;
import com.example.parser.domain.entity.TournamentResultEntity;
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

    public boolean processResults(
            List<ResultDto> results,
            Player player,
            Long tournamentId,
            double bonus,
            boolean isFinished
    ) {

        boolean found = false;

        for (ResultDto r : results) {

            if (isSamePlayer(player.getName(), r.getPlayer())) {
                found = true;

                if (isFinished) {

                    boolean isNight = bonus > 0;
                    double finalAmount = r.getTotal() + bonus;

                    TournamentResultEntity entity = TournamentResultEntity.builder()
                            .player(player)
                            .playerName(r.getPlayer())
                            .amount(finalAmount)
                            .date(LocalDate.parse(r.getDate()))
                            .tournamentId(tournamentId)
                            .isNight(isNight)
                            .bonus(bonus)
                            .build();

                    save(entity);
                }
            }
        }

        return found;
    }

    private boolean isSamePlayer(String n1, String n2) {
        String p1 = n1.replaceAll("\\s+", " ").trim().toLowerCase();
        String p2 = n2.replaceAll("\\s+", " ").trim().toLowerCase();

        String[] parts = p1.split(" ");

        for (String part : parts) {
            if (!p2.contains(part)) {
                return false;
            }
        }

        return true;
    }
}