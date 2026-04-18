package com.example.parser.modules.tournament.service;

import com.example.parser.core.dto.ResultDto;
import com.example.parser.core.dto.TournamentLinkResult;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.tournament.domain.Tournament;
import com.example.parser.modules.tournament.domain.TournamentLinkStatus;
import com.example.parser.modules.tournament.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TournamentLinkService {

    private final ResultService resultService;
    private final TournamentResultService tournamentResultService;
    private final TournamentSyncService tournamentSyncService;
    private final TournamentRepository tournamentRepository;
    private final ParticipationService participationService; // ✅ добавили

    public TournamentLinkResult process(String link, Player player) throws Exception {

        log.warn("=================================");
        log.warn("PROCESS START");
        log.warn("link={}", link);
        log.warn("player={}", player.getName());

        // 1. парсим
        ResultService.ParsedResult parsed = resultService.calculateAll(link);

        // 2. турнир НЕ начался
        if (parsed.getResults() == null || parsed.getResults().isEmpty()) {
            log.warn("RETURN → NOT_STARTED");
            return result(TournamentLinkStatus.NOT_STARTED, parsed);
        }

        log.warn("PARSED:");
        log.warn("tournamentId={}", parsed.getTournamentId());
        log.warn("isFinished={}", parsed.isFinished());
        log.warn("results size={}", parsed.getResults().size());

        // лог игроков
        for (ResultDto r : parsed.getResults()) {
            log.warn("RESULT → player='{}' total={}", r.getPlayer(), r.getTotal());
        }

        // 3. проверка участия (ТОЛЬКО через parsed)
        boolean userExists = participationService.isUserInParsed(parsed, player.getName());
        log.warn("USER EXISTS IN PARSED={}", userExists);

        if (!userExists) {
            log.warn("RETURN → NOT_PARTICIPATING");
            return result(TournamentLinkStatus.NOT_PARTICIPATING, parsed);
        }

        // 4. проверка БД (только статус)
        Tournament tournament = tournamentRepository.findByLink(link).orElse(null);
        log.warn("TOURNAMENT FROM DB: {}", tournament != null ? "FOUND" : "NOT FOUND");

        if (tournament != null) {

            log.warn("TOURNAMENT ID={}", tournament.getId());
            log.warn("TOURNAMENT processed={}", tournament.isProcessed());

            if (!tournament.isProcessed()) {
                log.warn("RETURN → ALREADY_TRACKED");
                return result(TournamentLinkStatus.ALREADY_TRACKED, parsed);
            }

            log.warn("RETURN → FINISHED");
            return result(TournamentLinkStatus.FINISHED, parsed);
        }

        // 5. новый турнир
        log.warn("NEW TOURNAMENT → syncing...");

        tournament = tournamentSyncService.sync(parsed, link);

        tournamentResultService.processResults(
                parsed.getResults(),
                player,
                tournament,
                parsed.getNightBonus(),
                parsed.isFinished()
        );

        if (parsed.isFinished()) {
            tournament.setProcessed(true);
            log.warn("RETURN → FINISHED");
            return result(TournamentLinkStatus.FINISHED, parsed);
        }

        log.warn("RETURN → TRACKING_STARTED");
        return result(TournamentLinkStatus.TRACKING_STARTED, parsed);
    }

    private TournamentLinkResult result(TournamentLinkStatus status,
                                        ResultService.ParsedResult parsed) {
        log.warn("FINAL STATUS={}", status);
        log.warn("=================================");
        return new TournamentLinkResult(status, parsed);
    }
}