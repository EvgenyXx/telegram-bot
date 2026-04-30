package com.example.parser.modules.player.service;

import com.example.parser.core.dto.PeriodStatsProjection;
import com.example.parser.modules.notification.repository.PlayerNotificationRepository;
import com.example.parser.modules.player.api.dto.*;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.tournament.application.TournamentResultService;
import com.example.parser.modules.tournament.persistence.repository.TournamentResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerStatsService {

    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;
    private final TournamentResultRepository tournamentResultRepository;
    private final PlayerNotificationRepository notificationRepository;

    public DashboardResponse getDashboard(UUID id) {
        Player player = playerService.getById(id);

        var lastResult = tournamentResultRepository.findTopByPlayerOrderByDateDesc(player)
                .map(r -> LastResultDto.builder()
                        .date(r.getDate().toString())
                        .amount(r.getAmount())
                        .tournamentLink(r.getTournament().getLink())
                        .build())
                .orElse(null);

        var nextNotification = notificationRepository.findNextTournament(player)
                .map(pn -> NextTournamentDto.builder()
                        .date(pn.getTournament().getDate().toString())
                        .time(pn.getTournament().getTime())
                        .link(pn.getTournament().getLink())
                        .hall(pn.getHall() != null ? pn.getHall().toString() : null)
                        .build())
                .orElse(null);

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
                .nextTournament(nextNotification)
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