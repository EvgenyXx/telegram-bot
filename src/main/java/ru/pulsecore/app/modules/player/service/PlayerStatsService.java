package ru.pulsecore.app.modules.player.service;

import ru.pulsecore.app.core.dto.PeriodStatsProjection;
import ru.pulsecore.app.modules.lineup.domain.Lineup;
import ru.pulsecore.app.modules.lineup.repository.LineupRepository;
import ru.pulsecore.app.modules.player.api.dto.*;
import ru.pulsecore.app.modules.player.domain.Player;
import ru.pulsecore.app.modules.tournament.application.TournamentResultService;
import ru.pulsecore.app.modules.tournament.persistence.repository.TournamentResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerStatsService {

    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;
    private final TournamentResultRepository tournamentResultRepository;
    private final LineupRepository lineupRepository;

    public DashboardResponse getDashboard(UUID id) {
        Player player = playerService.getById(id);
        String playerNameLower = player.getName().toLowerCase();

        var lastResult = tournamentResultRepository.findTopByPlayerOrderByDateDesc(player)
                .map(r -> LastResultDto.builder()
                        .date(r.getDate().toString())
                        .amount(r.getAmount())
                        .tournamentLink(r.getTournament().getLink())
                        .build())
                .orElse(null);

        // Ближайшие 2 дня из lineup
        LocalDate today = LocalDate.now();
        List<Lineup> lineups = lineupRepository.findByDateBetweenOrderByDateAscTimeAsc(
                today.plusDays(1), today.plusDays(2));

        // Группируем по датам
        Map<LocalDate, List<Lineup>> byDate = lineups.stream()
                .collect(Collectors.groupingBy(Lineup::getDate, LinkedHashMap::new, Collectors.toList()));

        LocalDate soonestDate = byDate.keySet().stream().min(LocalDate::compareTo).orElse(null);

        List<UpcomingLineupDto> upcomingLineups = new ArrayList<>();

        for (Map.Entry<LocalDate, List<Lineup>> entry : byDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<Lineup> dayLineups = entry.getValue();

            List<Lineup> myLineups = dayLineups.stream()
                    .filter(l -> l.getPlayers().toLowerCase().contains(playerNameLower))
                    .toList();

            if (!myLineups.isEmpty()) {
                // Игрок в составе — показываем его составы
                for (Lineup lineup : myLineups) {
                    upcomingLineups.add(UpcomingLineupDto.builder()
                            .date(lineup.getDate().toString())
                            .time(lineup.getTime())
                            .league(lineup.getLeague())
                            .inLineup(true)
                            .players(lineup.getPlayers())
                            .isSoon(date.equals(soonestDate))
                            .build());
                }
            } else {
                // Игрока нет ни в одном составе на эту дату
                upcomingLineups.add(UpcomingLineupDto.builder()
                        .date(date.toString())
                        .time(null)
                        .league(null)
                        .inLineup(false)
                        .players(null)
                        .isSoon(date.equals(soonestDate))
                        .build());
            }
        }

        var sub = player.getSubscription();
        SubscriptionInfoDto subInfo = sub != null && sub.isActiveNow()
                ? SubscriptionInfoDto.builder()
                  .active(true)
                  .expiresAt(sub.getExpiresAt().toString())
                  .build()
                : SubscriptionInfoDto.builder().active(false).build();

        return DashboardResponse.builder()
                .playerName(player.getName())
                .lastResult(lastResult)
                .upcomingLineups(upcomingLineups)
                .subscription(subInfo)
                .build();
    }

    public SumResponse getSum(UUID id, LocalDate start, LocalDate end) {
        Player player = playerService.getById(id);
        PeriodStatsProjection stats = tournamentResultService.getStatsByPeriod(player, start, end);
        return SumResponse.builder()
                .playerName(player.getName())
                .start(start.toString()).end(end.toString())
                .sum(stats != null ? stats.getSum() : 0)
                .average(stats != null ? stats.getAverage() : 0)
                .minusThreePercent(stats != null ? stats.getMinusThreePercent() : 0)
                .count(stats != null ? stats.getCount() : 0)
                .build();
    }

    public TournamentListResponse getTournaments(UUID id, LocalDate start, LocalDate end) {
        Player player = playerService.getById(id);
        var entities = tournamentResultService.getResultsByPeriod(player, start, end);
        List<TournamentListResponse.TournamentResultItem> tournaments = entities.stream()
                .map(e -> TournamentListResponse.TournamentResultItem.builder()
                        .date(e.getDate().toString()).amount(e.getAmount()).build())
                .collect(Collectors.toList());
        double sum = tournaments.stream().mapToDouble(TournamentListResponse.TournamentResultItem::getAmount).sum();
        return TournamentListResponse.builder()
                .playerName(player.getName())
                .start(start.toString()).end(end.toString())
                .count(tournaments.size()).sum(sum).tournaments(tournaments)
                .build();
    }
}