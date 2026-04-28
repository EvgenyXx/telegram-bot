package com.example.parser.modules.player.api;

import com.example.parser.core.dto.PeriodStatsProjection;
import com.example.parser.modules.player.api.dto.PlayerProfileResponse;
import com.example.parser.modules.player.api.dto.SumResponse;
import com.example.parser.modules.player.api.dto.TournamentListResponse;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.service.PlayerService;
import com.example.parser.modules.tournament.application.TournamentResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
public class PlayerApiController {

    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;

    @GetMapping("/code/{code}")
    public PlayerProfileResponse getByCode(@PathVariable String code) {
        Player player = resolvePlayer(code);
        return PlayerProfileResponse.builder()
                .id(player.getId())
                .name(player.getName())
                .build();
    }

    @GetMapping("/code/{code}/sum")
    public SumResponse getSumByPeriod(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        Player player = resolvePlayer(code);
        PeriodStatsProjection stats = tournamentResultService.getStatsByPeriod(player, start, end);

        return SumResponse.builder()
                .playerName(player.getName())
                .start(start.toString())
                .end(end.toString())
                .sum(stats != null ? stats.getSum() : 0)
                .average(stats != null ? stats.getAverage() : 0)
                .minusThreePercent(stats != null ? stats.getMinusThreePercent() : 0)
                .count(stats != null ? stats.getCount() : 0)
                .build();
    }

    @GetMapping("/code/{code}/tournaments")
    public TournamentListResponse getTournamentsByPeriod(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        Player player = resolvePlayer(code);
        var entities = tournamentResultService.getResultsByPeriod(player, start, end);

        List<TournamentListResponse.TournamentResultItem> tournaments = entities.stream()
                .map(e -> TournamentListResponse.TournamentResultItem.builder()
                        .date(e.getDate().toString())
                        .amount(e.getAmount())
                        .build())
                .collect(Collectors.toList());

        double sum = tournaments.stream().mapToDouble(TournamentListResponse.TournamentResultItem::getAmount).sum();

        return TournamentListResponse.builder()
                .playerName(player.getName())
                .start(start.toString())
                .end(end.toString())
                .count(tournaments.size())
                .sum(sum)
                .tournaments(tournaments)
                .build();
    }

    private Player resolvePlayer(String code) {
        return playerService.findByAccessCode(code)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Неверный код доступа"));
    }
}