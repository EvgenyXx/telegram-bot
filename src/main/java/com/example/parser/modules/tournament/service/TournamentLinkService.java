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
@Transactional//todo пофиксить баг когда кидаю ссылку на турнир который уже отслеживается не менялся статус 
public class TournamentLinkService {//todo добавить файл с полным описанием как все работает в начале и просто как кнопку

    private final ResultService resultService;
    private final TournamentResultService tournamentResultService;
    private final TournamentSyncService tournamentSyncService;
    private final TournamentRepository tournamentRepository;

    public TournamentLinkResult process(String link, Player player) throws Exception {

        Tournament existing = tournamentRepository.findByLink(link).orElse(null);

        // 🔥 если турнир уже есть
        if (existing != null) {

            ResultService.ParsedResult parsed = resultService.calculateAll(link);

            // уже обработан → просто показываем
            if (existing.isProcessed()) {
                return new TournamentLinkResult(
                        TournamentLinkStatus.FINISHED,
                        parsed
                );
            }

            // 🔥 проверяем пользователя
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

            boolean found = tournamentResultService.processResults(
                    parsed.getResults(),
                    player,
                    parsed.getTournamentId(),
                    parsed.getNightBonus(),
                    parsed.isFinished()
            );

            if (!found) {
                return new TournamentLinkResult(
                        TournamentLinkStatus.NOT_PARTICIPATING,
                        parsed
                );
            }

            return new TournamentLinkResult(
                    TournamentLinkStatus.ALREADY_TRACKED,
                    parsed
            );

        }

        // 🔽 дальше как было (новый турнир)

        ResultService.ParsedResult parsed = resultService.calculateAll(link);

        Tournament tournament = tournamentSyncService.sync(parsed, link);

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

        boolean found = tournamentResultService.processResults(
                parsed.getResults(),
                player,
                parsed.getTournamentId(),
                parsed.getNightBonus(),
                parsed.isFinished()
        );

        if (!found) {
            return new TournamentLinkResult(
                    TournamentLinkStatus.NOT_PARTICIPATING,
                    parsed
            );
        }

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