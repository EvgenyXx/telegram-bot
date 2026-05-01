package com.example.parser.modules.tournament.application;


import com.example.parser.core.dto.PeriodStatsProjection;
import com.example.parser.core.dto.ResultDto;
import com.example.parser.modules.shared.exception.TournamentNotFoundException;
import com.example.parser.modules.tournament.extraction.TournamentResultNotFoundException;
import com.example.parser.modules.tournament.persistence.entity.TournamentEntity;
import com.example.parser.modules.tournament.persistence.entity.TournamentResultEntity;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.tournament.persistence.repository.TournamentRepository;
import com.example.parser.modules.tournament.persistence.repository.TournamentResultRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentResultService {

    private final TournamentResultRepository tournamentResultRepository;
    private final TournamentRepository tournamentRepository;

    // 🔥 ОБНОВЛЕНО
    @Transactional
    public void updateResult(Long id, Double amount, Double bonus) {
        TournamentResultEntity result = tournamentResultRepository.findById(id)
                .orElseThrow(() -> new TournamentResultNotFoundException(id));
        if (amount != null) result.setAmount(amount);
        if (bonus != null) result.setBonus(bonus);
        tournamentResultRepository.save(result);
    }

    public void save(TournamentResultEntity entity) {
        boolean exists = tournamentResultRepository.existsByPlayerAndTournament_ExternalId(
                entity.getPlayer(),
                entity.getTournament().getExternalId()
        );

        if (exists) {
            return;
        }

        try {
            tournamentResultRepository.save(entity);
        } catch (Exception e) {
            log.error("SAVE ERROR: player={}, tournament={}",
                    entity.getPlayer().getName(),
                    entity.getTournament().getExternalId(), e);
        }
    }

    public List<TournamentResultEntity> getResultsByPeriod(Player player, LocalDate start, LocalDate end) {
        return tournamentResultRepository.findByPlayerAndDateBetweenOrderByDateAsc(player, start, end);
    }

    public PeriodStatsProjection getStatsByPeriod(Player player, LocalDate start, LocalDate end) {
        return tournamentResultRepository.getStats(player, start, end);
    }



    public void processResults(List<ResultDto> results,
                               Player player,
                               TournamentEntity tournament,
                               double bonus,
                               boolean isFinished) {



        for (ResultDto r : results) {

            boolean same = isSamePlayer(player.getName(), r.getPlayer());

            if (same) {


                if (isFinished) {

                    boolean isNight = bonus > 0;

                    // ✅ единая логика
                    double finalAmount = r.getTotal();

                    TournamentResultEntity entity = TournamentResultEntity.builder()
                            .player(player)
                            .playerName(r.getPlayer())
                            .amount(finalAmount)
                            .date(LocalDate.parse(r.getDate()))
                            .tournament(tournament)
                            .isNight(isNight)
                            .bonus(bonus)
                            .build();

                    save(entity);
                }
            }
        }

    }


    public boolean processResults(List<ResultDto> results,
                                  Player player,
                                  Long tournamentId,
                                  double bonus,
                                  boolean isFinished) {

        boolean found = false;

        // 👉 получаем Tournament один раз
        TournamentEntity tournament = tournamentRepository
                .findByExternalId(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));//todo добавить в исключения

        for (ResultDto r : results) {

            boolean same = isSamePlayer(player.getName(), r.getPlayer());

            if (same) {
                found = true;

                if (isFinished) {

                    boolean isNight = bonus > 0;
                    double finalAmount = r.getTotal();

                    TournamentResultEntity entity = TournamentResultEntity.builder()
                            .player(player)
                            .playerName(r.getPlayer())
                            .amount(finalAmount)
                            .date(LocalDate.parse(r.getDate()))
                            .tournament(tournament)
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
        return name.toLowerCase()
                .replaceAll("[^а-яa-z\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }


}