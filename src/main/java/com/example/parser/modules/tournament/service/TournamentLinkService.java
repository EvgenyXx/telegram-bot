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

    public TournamentLinkResult process(String link, Player player) throws Exception {

        log.warn("=================================");
        log.warn("PROCESS START");
        log.warn("link={}", link);
        log.warn("player={}", player.getName());

        // 1. парсим
        ResultService.ParsedResult parsed = resultService.calculateAll(link);

        log.warn("PARSED:");
        log.warn("tournamentId={}", parsed.getTournamentId());
        log.warn("isFinished={}", parsed.isFinished());
        log.warn("results size={}", parsed.getResults() != null ? parsed.getResults().size() : null);

        // 👉 лог всех игроков
        if (parsed.getResults() != null) {
            for (ResultDto r : parsed.getResults()) {
                log.warn("RESULT → player='{}' total={}", r.getPlayer(), r.getTotal());
            }
        }

        // 2. ищем турнир
        Tournament tournament = tournamentRepository.findByLink(link).orElse(null);

        log.warn("TOURNAMENT FROM DB: {}", tournament != null ? "FOUND" : "NOT FOUND");

        // =========================
        // ТУРНИР УЖЕ ЕСТЬ
        // =========================
        if (tournament != null) {

            log.warn("TOURNAMENT ID={}", tournament.getId());
            log.warn("TOURNAMENT processed={}", tournament.isProcessed());

            boolean userExists = tournamentResultService.exists(player, tournament);

            log.warn("USER EXISTS IN DB={}", userExists);

            // ❌ пользователь не участвует
            if (!userExists) {
                log.warn("RETURN → NOT_PARTICIPATING (DB CHECK)");
                return result(TournamentLinkStatus.NOT_PARTICIPATING, parsed);
            }

            if (!tournament.isProcessed()) {
                log.warn("RETURN → ALREADY_TRACKED");
                return result(TournamentLinkStatus.ALREADY_TRACKED, parsed);
            }

            log.warn("RETURN → FINISHED");
            return result(TournamentLinkStatus.FINISHED, parsed);
        }

        // =========================
        // НОВЫЙ ТУРНИР
        // =========================

        log.warn("NEW TOURNAMENT → syncing...");

        tournament = tournamentSyncService.sync(parsed, link);

        boolean found = tournamentResultService.processResults(
                parsed.getResults(),
                player,
                tournament,
                parsed.getNightBonus(),
                parsed.isFinished()
        );

        log.warn("USER FOUND IN PARSED={}", found);

        if (!found) {
            log.warn("RETURN → NOT_PARTICIPATING (PARSED CHECK)");
            return result(TournamentLinkStatus.NOT_PARTICIPATING, parsed);
        }

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