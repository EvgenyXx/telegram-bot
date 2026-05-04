package ru.pulsecore.app.modules.tournament.application;

import ru.pulsecore.app.core.dto.TournamentLinkResult;
import ru.pulsecore.app.modules.player.domain.Player;
import ru.pulsecore.app.modules.tournament.persistence.entity.TournamentEntity;
import ru.pulsecore.app.modules.tournament.persistence.entity.TournamentLinkStatus;
import ru.pulsecore.app.modules.tournament.persistence.repository.TournamentRepository;
import ru.pulsecore.app.modules.tournament.domain.ParsedResult;
import ru.pulsecore.app.modules.tournament.domain.TournamentStatus;
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
    private final ParticipationService participationService;

    public TournamentLinkResult process(String link, Player player) throws Exception {

        ParsedResult parsed = resultService.calculateAll(link);

        // турнир НЕ начался (оставляем твою старую логику)
        if (parsed.getResults() == null || parsed.getResults().isEmpty()) {
            return result(TournamentLinkStatus.NOT_STARTED, parsed);
        }

        // проверка участия (ТОЛЬКО через parsed)
        boolean userExists = participationService.isUserInParsed(parsed, player.getName());
        if (!userExists) {
            return result(TournamentLinkStatus.NOT_PARTICIPATING, parsed);
        }

        // проверка БД (только статус)
        TournamentEntity tournament = tournamentRepository.findByLink(link).orElse(null);
        if (tournament != null) {
            if (!tournament.isProcessed()) {
                return result(TournamentLinkStatus.ALREADY_TRACKED, parsed);
            }
            return result(TournamentLinkStatus.FINISHED, parsed);
        }

        // новый турнир
        tournament = tournamentSyncService.sync(parsed, link);

        tournamentResultService.processResults(
                parsed.getResults(),
                player,
                tournament,
                parsed.getNightBonus(),
                parsed.getStatus() == TournamentStatus.FINISHED
        );

        if (parsed.getStatus() == TournamentStatus.FINISHED) {
            tournament.setProcessed(true);
            return result(TournamentLinkStatus.FINISHED, parsed);
        }

        return result(TournamentLinkStatus.TRACKING_STARTED, parsed);
    }

    private TournamentLinkResult result(TournamentLinkStatus status,
                                        ParsedResult parsed) {
        log.debug("TournamentLink status={}", status);
        return new TournamentLinkResult(status, parsed);
    }
}