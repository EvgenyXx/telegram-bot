package com.example.parser.tournament;

import com.example.parser.domain.dto.FullStatsDto;
import com.example.parser.domain.dto.FullStatsProjection;
import com.example.parser.domain.dto.PeriodStatsProjection;
import com.example.parser.domain.dto.ResultDto;
import com.example.parser.player.Player;
import com.example.parser.domain.entity.TournamentResultEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentResultService {

    private final TournamentResultRepository repository;

    public void save(TournamentResultEntity entity) {

        log.warn("════════ SAVE START ════════");
        log.warn("SAVE → player={}, tournamentId={}, amount={}",
                entity.getPlayerName(),
                entity.getTournamentId(),
                entity.getAmount()
        );

        boolean exists = repository.existsByPlayerAndTournamentId(
                entity.getPlayer(),
                entity.getTournamentId()
        );

        log.warn("SAVE → existsByPlayerAndTournamentId={}", exists);

        if (exists) {
            log.warn("SAVE SKIP → already exists");
            return;
        }

        try {
            repository.save(entity);
            log.warn("SAVE SUCCESS");
        } catch (Exception e) {
            log.error("❌ SAVE ERROR", e);
        }

        log.warn("════════ SAVE END ════════");
    }

    public List<TournamentResultEntity> getResultsByPeriod(Player player, LocalDate start, LocalDate end) {

        log.warn("GET RESULTS PERIOD → player={}, start={}, end={}",
                player.getName(), start, end);

        List<TournamentResultEntity> list =
                repository.findByPlayerAndDateBetweenOrderByDateAsc(player, start, end);

        log.warn("GET RESULTS PERIOD → size={}", list.size());

        return list;
    }

    public PeriodStatsProjection getStatsByPeriod(Player player, LocalDate start, LocalDate end) {

        log.warn("GET STATS PERIOD → player={}, start={}, end={}",
                player.getName(), start, end);

        PeriodStatsProjection stats = repository.getStats(player, start, end);

        log.warn("GET STATS PERIOD → result={}", stats);

        return stats;
    }

    public FullStatsDto getFullStats(Player player) {

        log.warn("GET FULL STATS → player={}", player.getName());

        FullStatsProjection stats = repository.getFullStats(player);

        if (stats == null) {
            log.warn("GET FULL STATS → stats=null");
            return null;
        }

        log.warn("GET FULL STATS → count={}, sum={}, avg={}",
                stats.getCount(),
                stats.getSum(),
                stats.getAvg()
        );

        if (stats.getCount() == 0) {
            log.warn("GET FULL STATS → count=0");
            return null;
        }

        long count = stats.getCount();
        double sum = stats.getSum() != null ? stats.getSum() : 0;
        double avg = stats.getAvg() != null ? stats.getAvg() : 0;

        return new FullStatsDto(count, sum, avg);
    }

    public boolean processResults(List<ResultDto> results,
                                  Player player,
                                  Long tournamentId,
                                  double bonus,
                                  boolean isFinished) {

        log.warn("════════ PROCESS START ════════");
        log.warn("PLAYER = {}", player.getName());
        log.warn("TOURNAMENT ID = {}", tournamentId);
        log.warn("RESULTS SIZE = {}", results.size());
        log.warn("BONUS = {}", bonus);
        log.warn("FINISHED = {}", isFinished);

        boolean found = false;

        for (ResultDto r : results) {

            log.warn("ITERATE RESULT → player={}, place={}, total={}",
                    r.getPlayer(),
                    r.getPlace(),
                    r.getTotal()
            );

            boolean same = isSamePlayer(player.getName(), r.getPlayer());

            log.warn("COMPARE → {} vs {} = {}",
                    player.getName(),
                    r.getPlayer(),
                    same
            );

            if (same) {

                log.warn("✅ FOUND PLAYER IN RESULTS");

                found = true;

                if (isFinished) {

                    boolean isNight = bonus > 0;
                    double finalAmount = r.getTotal() + bonus;

                    log.warn("CALC FINAL → total={}, bonus={}, final={}",
                            r.getTotal(),
                            bonus,
                            finalAmount
                    );

                    TournamentResultEntity entity = TournamentResultEntity.builder()
                            .player(player)
                            .playerName(r.getPlayer())
                            .amount(finalAmount)
                            .date(LocalDate.parse(r.getDate()))
                            .tournamentId(tournamentId)
                            .isNight(isNight)
                            .bonus(bonus)
                            .build();

                    log.warn("ENTITY BUILD → player={}, date={}, isNight={}",
                            entity.getPlayerName(),
                            entity.getDate()

                    );

                    save(entity);
                }
            }
        }

        log.warn("PROCESS RESULT → found={}", found);
        log.warn("════════ PROCESS END ════════");

        return found;
    }

    private boolean isSamePlayer(String n1, String n2) {

        log.warn("IS SAME PLAYER → n1={}, n2={}", n1, n2);

        if (n1 == null || n2 == null) {
            log.warn("IS SAME PLAYER → one is null");
            return false;
        }

        String p1 = normalizeName(n1);
        String p2 = normalizeName(n2);

        log.warn("NORMALIZED → p1={}, p2={}", p1, p2);

        String[] parts1 = p1.split(" ");
        String[] parts2 = p2.split(" ");

        int matches = 0;

        for (String part1 : parts1) {
            for (String part2 : parts2) {
                if (part1.equals(part2)) {
                    matches++;
                }
            }
        }

        log.warn("MATCH COUNT = {}", matches);

        boolean result = matches >= 2;

        log.warn("IS SAME PLAYER RESULT = {}", result);

        return result;
    }

    private String normalizeName(String name) {

        String normalized = name.toLowerCase()
                .replaceAll("[^а-яa-z\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();

        log.warn("NORMALIZE → {} → {}", name, normalized);

        return normalized;
    }
}