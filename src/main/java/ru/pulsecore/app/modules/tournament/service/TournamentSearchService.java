package ru.pulsecore.app.modules.tournament.service;

import ru.pulsecore.app.core.dto.TournamentDto;
import ru.pulsecore.app.modules.lineup.client.MastersApiClient;
import ru.pulsecore.app.modules.player.domain.Player;
import ru.pulsecore.app.modules.player.service.PlayerService;
import ru.pulsecore.app.modules.tournament.api.dto.TournamentSearchResult;
import ru.pulsecore.app.modules.tournament.persistence.repository.TournamentResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TournamentSearchService {

    private final MastersApiClient mastersApiClient;
    private final TournamentResultRepository tournamentResultRepository;
    private final PlayerService playerService;


    public List<TournamentDto> findByDateAndPlayer(String date, String playerName) {
        final String searchName = playerName.toLowerCase();
        return mastersApiClient.loadTournaments(date).stream()
                .filter(t -> t.getPlayers() != null && t.getPlayers().stream()
                        .anyMatch(p -> p.toLowerCase().contains(searchName)))
                .toList();
    }

    public List<TournamentSearchResult> findByDateAndPlayerWithStatus(String date, String playerId) {
        Player player = playerService.findById(UUID.fromString(playerId));
        if (player == null) return List.of();

        List<TournamentDto> tournaments = findByDateAndPlayer(date, player.getName());
        return tournaments.stream()
                .map(t -> TournamentSearchResult.builder()
                        .tournament(t)
                        .saved(tournamentResultRepository.existsByPlayerAndTournament_ExternalId(player, t.getId()))
                        .build())
                .toList();
    }

    // TournamentSearchService.java — добавь метод
    public List<TournamentSearchResult> findByDateRangeAndPlayerWithStatus(String startDate, String endDate, String playerId) {
        Player player = playerService.findById(UUID.fromString(playerId));
        if (player == null) return List.of();

        List<TournamentDto> tournaments = findByDateRangeAndPlayer(startDate, endDate, player.getName());
        return tournaments.stream()
                .map(t -> TournamentSearchResult.builder()
                        .tournament(t)
                        .saved(tournamentResultRepository.existsByPlayerAndTournament_ExternalId(player, t.getId()))
                        .build())
                .toList();
    }

    public List<TournamentDto> findByDateRangeAndPlayer(String startDate, String endDate, String playerName) {
        final String searchName = playerName.toLowerCase();
        List<TournamentDto> allTournaments = new ArrayList<>();

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        LocalDate current = start;
        while (!current.isAfter(end)) {
            String dateStr = current.toString();
            try {
                List<TournamentDto> dayTournaments = mastersApiClient.loadTournaments(dateStr);
                List<TournamentDto> filtered = dayTournaments.stream()
                        .filter(t -> t.getPlayers() != null && t.getPlayers().stream()
                                .anyMatch(p -> p.toLowerCase().contains(searchName)))
                        .toList();
                allTournaments.addAll(filtered);
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Failed to load tournaments for {}: {}", dateStr, e.getMessage());
            }
            current = current.plusDays(1);
        }

        log.info("Tournaments found for period {}-{}: {}", startDate, endDate, allTournaments.size());
        return allTournaments;
    }
}