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

        // 1. уже есть в системе
        Tournament existing = tournamentRepository.findByLink(link).orElse(null);
        if (existing != null) {
            return result(TournamentLinkStatus.ALREADY_TRACKED, null);
        }

        // 2. парсинг
        ResultService.ParsedResult parsed = resultService.calculateAll(link);

        // 3. турнир ещё не начался (нет результатов)
        if (isEmpty(parsed)) {
            tournamentSyncService.sync(parsed, link);
            return result(TournamentLinkStatus.ALREADY_TRACKED, parsed);
        }

        // 4. синхронизация
        Tournament tournament = tournamentSyncService.sync(parsed, link);

        // 5. уже обработан
        if (tournament.isProcessed()) {
            return result(TournamentLinkStatus.ALREADY_TRACKED, parsed);
        }

        // 6. уже есть у пользователя
        if (tournamentResultService.exists(player, parsed.getTournamentId())) {
            return result(TournamentLinkStatus.USER_ALREADY_EXISTS, parsed);
        }

        // 7. проверка участия + сохранение
        boolean found = tournamentResultService.processResults(
                parsed.getResults(),
                player,
                parsed.getTournamentId(),
                parsed.getNightBonus(),
                parsed.isFinished()
        );

        if (!found) {
            return result(TournamentLinkStatus.NOT_PARTICIPATING, parsed);
        }

        // 8. финальный статус
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