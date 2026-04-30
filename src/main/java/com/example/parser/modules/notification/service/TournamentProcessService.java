package com.example.parser.modules.notification.service;

import com.example.parser.core.dto.ResultDto;
import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.service.PlayerService;
import com.example.parser.modules.shared.exception.PlayerNotFoundException;
import com.example.parser.modules.shared.exception.TournamentParseException;
import com.example.parser.modules.shared.exception.UnauthorizedException;
import com.example.parser.modules.tournament.application.ResultService;
import com.example.parser.modules.tournament.application.TournamentResultService;
import com.example.parser.modules.tournament.domain.ParsedResult;
import com.example.parser.modules.tournament.dto.AddTournamentResponse;
import com.example.parser.modules.tournament.persistence.entity.TournamentEntity;
import com.example.parser.modules.tournament.persistence.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentProcessService {

    private final TournamentResultService tournamentResultService;
    private final ResultService resultService;
    private final TournamentRepository tournamentRepository;
    private final PlayerService playerService;

    @Transactional
    public void processTournament(List<PlayerNotification> notifications,
                                  ParsedResult parsed) {
        if (notifications == null || notifications.isEmpty()) {
            log.warn("⏭ processTournament skip: empty notifications");
            return;
        }
        TournamentEntity tournament = notifications.get(0).getTournament();
        if (tournament == null) {
            log.warn("⏭ processTournament skip: tournament is null");
            return;
        }
        log.info("🏁 process finish: tournamentId={}, users={}",
                parsed.getTournamentId(), notifications.size());

        int processed = 0, foundCount = 0;
        List<ResultDto> resultDto = parsed.getResults();

        for (PlayerNotification pn : notifications) {
            Player player = pn.getPlayer();
            if (player == null) continue;
            processed++;
            boolean found = tournamentResultService.processResults(
                    resultDto, player, parsed.getTournamentId(),
                    parsed.getNightBonus(), true);
            if (found) foundCount++;
        }
        tournament.setFinished(true);
        log.info("✅ process finish done: tournamentId={}, processed={}, found={}",
                tournament.getExternalId(), processed, foundCount);
    }

    public AddTournamentResponse processByUrl(String url, String playerId) {
        if (playerId == null) throw new UnauthorizedException();

        Player player = playerService.findById(UUID.fromString(playerId));
        if (player == null) throw new PlayerNotFoundException(playerId);

        ParsedResult parsed;
        try {
            parsed = resultService.calculateAll(url);
        } catch (Exception e) {
            throw new TournamentParseException(url, e);
        }

        tournamentRepository.findByExternalId(parsed.getTournamentId())
                .orElseGet(() -> tournamentRepository.save(TournamentEntity.builder()
                        .externalId(parsed.getTournamentId())
                        .link(url)
                        .build()));

        tournamentResultService.processResults(
                parsed.getResults(), player, parsed.getTournamentId(),
                parsed.getNightBonus(), parsed.isFinished());

        return AddTournamentResponse.builder()
                .message("Турнир обработан")
                .tournamentId(parsed.getTournamentId())
                .resultsCount(parsed.getResults().size())
                .results(parsed.getResults())
                .build();
    }
}