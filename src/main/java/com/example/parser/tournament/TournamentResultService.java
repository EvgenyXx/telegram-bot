package com.example.parser.tournament;

import com.example.parser.domain.dto.FullStatsDto;
import com.example.parser.domain.dto.FullStatsProjection;
import com.example.parser.domain.dto.PeriodStatsProjection;
import com.example.parser.domain.dto.ResultDto;
import com.example.parser.domain.entity.Tournament;
import com.example.parser.domain.entity.TournamentResultEntity;
import com.example.parser.player.Player;
import com.example.parser.lineup.TournamentRepository;
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
    private final TournamentRepository tournamentRepository;

    // 🔥 ОБНОВЛЕНО
    public void updateAmount(Player player, Long tournamentId, double amount) {
        TournamentResultEntity entity = repository
                .findByPlayerAndTournament_ExternalId(player, tournamentId)
                .orElseThrow(() -> new RuntimeException("Результат не найден"));

        entity.setAmount(amount);
        repository.save(entity);
    }

    public void save(TournamentResultEntity entity) {
        boolean exists = repository.existsByPlayerAndTournament_ExternalId(
                entity.getPlayer(),
                entity.getTournament().getExternalId()
        );

        log.warn("SAVE → exists={}", exists);

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
    }

    public List<TournamentResultEntity> getResultsByPeriod(Player player, LocalDate start, LocalDate end) {
        return repository.findByPlayerAndDateBetweenOrderByDateAsc(player, start, end);
    }

    public PeriodStatsProjection getStatsByPeriod(Player player, LocalDate start, LocalDate end) {
        return repository.getStats(player, start, end);
    }

    public FullStatsDto getFullStats(Player player) {
        FullStatsProjection stats = repository.getFullStats(player);

        if (stats == null || stats.getCount() == 0) {
            log.warn("GET FULL STATS → empty");
            return null;
        }

        long count = stats.getCount();
        double sum = stats.getSum() != null ? stats.getSum() : 0;
        double avg = stats.getAvg() != null ? stats.getAvg() : 0;

        return new FullStatsDto(count, sum, avg);
    }

    // 🔥 ГЛАВНОЕ ИСПРАВЛЕНИЕ
    public boolean processResults(List<ResultDto> results,
                                  Player player,
                                  Long tournamentId,
                                  double bonus,
                                  boolean isFinished) {

        boolean found = false;

        // 👉 получаем Tournament один раз
        Tournament tournament = tournamentRepository
                .findByExternalId(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        for (ResultDto r : results) {

            boolean same = isSamePlayer(player.getName(), r.getPlayer());

            if (same) {
                log.warn("✅ FOUND PLAYER IN RESULTS");
                found = true;

                if (isFinished) {

                    boolean isNight = bonus > 0;
                    double finalAmount = r.getTotal();

                    TournamentResultEntity entity = TournamentResultEntity.builder()
                            .player(player)
                            .playerName(r.getPlayer())
                            .amount(finalAmount)
                            .date(LocalDate.parse(r.getDate()))
                            .tournament(tournament) // 🔥 ВМЕСТО tournamentId
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
        if (n1 == null || n2 == null) return false;

        String p1 = normalizeName(n1);
        String p2 = normalizeName(n2);

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

        return matches >= 2;
    }

    private String normalizeName(String name) {
        String normalized = name.toLowerCase()
                .replaceAll("[^а-яa-z\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();

        log.warn("NORMALIZE → {} → {}", name, normalized);

        return normalized;
    }

    // 🔥 ОБНОВЛЕНО
    public boolean exists(Player player, Long tournamentId) {
        return repository.existsByPlayerAndTournament_ExternalId(player, tournamentId);
    }
}