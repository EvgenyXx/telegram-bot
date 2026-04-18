package com.example.parser.modules.tournament.service;


import com.example.parser.core.dto.TournamentLinkResult;
import com.example.parser.modules.tournament.domain.Tournament;
import com.example.parser.modules.player.domain.Player;
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

        Tournament existing = tournamentRepository.findByLink(link).orElse(null);
        ResultService.ParsedResult parsed = resultService.calculateAll(link);

        // 🔥 определяем участие пользователя
        boolean isParticipant = resultService.isUserInTournament(parsed, player);

        // =========================
        // ✅ ТУРНИР УЖЕ ЕСТЬ В БАЗЕ
        // =========================
        if (existing != null) {

            // ❌ не участвует
            if (!isParticipant) {
                return new TournamentLinkResult(
                        TournamentLinkStatus.NOT_PARTICIPATING,
                        parsed
                );
            }

            boolean alreadyExists = tournamentResultService.exists(
                    player,
                    parsed.getTournamentId()
            );

            // ✅ уже есть у пользователя
            if (alreadyExists) {

                if (parsed.isFinished()) {
                    return new TournamentLinkResult(
                            TournamentLinkStatus.USER_ALREADY_EXISTS,
                            parsed
                    );
                }

                return new TournamentLinkResult(
                        TournamentLinkStatus.ALREADY_TRACKED,
                        parsed
                );
            }

            // 🆕 участвует, но ещё не добавлен
            tournamentResultService.processResults(
                    parsed.getResults(),
                    player,
                    parsed.getTournamentId(),
                    parsed.getNightBonus(),
                    parsed.isFinished()
            );

            if (parsed.isFinished()) {
                return new TournamentLinkResult(
                        TournamentLinkStatus.FINISHED,
                        parsed
                );
            }

            return new TournamentLinkResult(
                    TournamentLinkStatus.TRACKING_STARTED,
                    parsed
            );
        }

        // =========================
        // 🆕 НОВЫЙ ТУРНИР
        // =========================

        Tournament tournament = tournamentSyncService.sync(parsed, link);

        // ❌ не участвует
        if (!isParticipant) {
            return new TournamentLinkResult(
                    TournamentLinkStatus.NOT_PARTICIPATING,
                    parsed
            );
        }

        boolean alreadyExists = tournamentResultService.exists(
                player,
                parsed.getTournamentId()
        );

        if (alreadyExists) {
            return new TournamentLinkResult(
                    TournamentLinkStatus.USER_ALREADY_EXISTS,
                    parsed
            );
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
            return new TournamentLinkResult(
                    TournamentLinkStatus.FINISHED,
                    parsed
            );
        }

        return new TournamentLinkResult(
                TournamentLinkStatus.TRACKING_STARTED,
                parsed
        );
    }
}