package com.example.parser.modules.tournament.service;

import com.example.parser.core.dto.TournamentLinkResult;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.tournament.domain.Tournament;
import com.example.parser.modules.tournament.domain.TournamentLinkStatus;
import com.example.parser.modules.tournament.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TournamentLinkService {

    private final ResultService resultService;
    private final TournamentResultService tournamentResultService;
    private final TournamentSyncService tournamentSyncService;
    private final TournamentRepository tournamentRepository;

    public TournamentLinkResult process(String link, Player player) throws Exception {

        // 1. парсинг ВСЕГДА
        ResultService.ParsedResult parsed = resultService.calculateAll(link);

        Tournament existing = tournamentRepository.findByLink(link).orElse(null);

        // 🔥 проверка участия
        boolean isParticipant = resultService.isUserInTournament(parsed, player);

        // =========================
        // ТУРНИР УЖЕ ЕСТЬ
        // =========================
        if (existing != null) {

            if (!isParticipant) {
                return result(TournamentLinkStatus.NOT_PARTICIPATING, parsed);
            }

            if (tournamentResultService.exists(player, parsed.getTournamentId())) {
                return result(TournamentLinkStatus.USER_ALREADY_EXISTS, parsed);
            }

            if (parsed.isFinished()) {
                return result(TournamentLinkStatus.FINISHED, parsed);
            }

            return result(TournamentLinkStatus.ALREADY_TRACKED, parsed);
        }

        // =========================
        // НОВЫЙ ТУРНИР
        // =========================

        if (isEmpty(parsed)) {
            tournamentSyncService.sync(parsed, link);
            return result(TournamentLinkStatus.ALREADY_TRACKED, parsed);
        }

        Tournament tournament = tournamentSyncService.sync(parsed, link);

        if (!isParticipant) {
            return result(TournamentLinkStatus.NOT_PARTICIPATING, parsed);
        }

        if (tournamentResultService.exists(player, parsed.getTournamentId())) {
            return result(TournamentLinkStatus.USER_ALREADY_EXISTS, parsed);
        }

        tournamentResultService.processResults(
                parsed.getResults(),
                player,
                parsed.getTournamentId(),
                parsed.getNightBonus(),
                parsed.isFinished()
        );

        if (parsed.isFinished()) {
            tournament.setProcessed(true);
            return result(TournamentLinkStatus.FINISHED, parsed);
        }

        return result(TournamentLinkStatus.TRACKING_STARTED, parsed);
    }

    // =========================
    // HELPERS
    // =========================

    private boolean isEmpty(ResultService.ParsedResult parsed) {
        return parsed.getResults() == null || parsed.getResults().isEmpty();
    }

    private TournamentLinkResult result(TournamentLinkStatus status,
                                        ResultService.ParsedResult parsed) {
        return new TournamentLinkResult(status, parsed);
    }
}