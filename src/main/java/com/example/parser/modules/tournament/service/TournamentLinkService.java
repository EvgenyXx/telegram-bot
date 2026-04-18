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

        // 1. парсим
        ResultService.ParsedResult parsed = resultService.calculateAll(link);

        // 2. ищем турнир
        Tournament tournament = tournamentRepository.findByLink(link).orElse(null);

        // =========================
        // ТУРНИР УЖЕ ЕСТЬ
        // =========================
        if (tournament != null) {

            boolean userExists = tournamentResultService.exists(player, tournament);

            // ❌ не участвует
            if (!userExists) {
                return result(TournamentLinkStatus.NOT_PARTICIPATING, parsed);
            }

            // ✅ уже сохранён
            if (tournament.isProcessed()) {
                return result(TournamentLinkStatus.FINISHED, parsed);
            }

            // 🔄 уже отслеживается
            return result(TournamentLinkStatus.ALREADY_TRACKED, parsed);
        }

        // =========================
        // НОВЫЙ ТУРНИР
        // =========================

        tournament = tournamentSyncService.sync(parsed, link);

        boolean found = tournamentResultService.processResults(
                parsed.getResults(),
                player,
                tournament,
                parsed.getNightBonus(),
                parsed.isFinished()
        );

        // ❌ не участвует
        if (!found) {
            return result(TournamentLinkStatus.NOT_PARTICIPATING, parsed);
        }

        // ✅ завершён
        if (parsed.isFinished()) {
            tournament.setProcessed(true);
            return result(TournamentLinkStatus.FINISHED, parsed);
        }

        // 🔄 начали отслеживание
        return result(TournamentLinkStatus.TRACKING_STARTED, parsed);
    }

    private TournamentLinkResult result(TournamentLinkStatus status,
                                        ResultService.ParsedResult parsed) {
        return new TournamentLinkResult(status, parsed);
    }
}