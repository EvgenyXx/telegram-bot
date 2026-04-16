package com.example.parser.modules.tournament.service;


import com.example.parser.core.dto.TournamentLinkResult;
import com.example.parser.modules.tournament.domain.Tournament;
import com.example.parser.modules.player.domain.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TournamentLinkService {

    private final ResultService resultService;
    private final TournamentResultService tournamentResultService;
    private final TournamentSyncService tournamentSyncService;

    public TournamentLinkResult process(String link, Player player) throws Exception {

        // 1. парсинг
        ResultService.ParsedResult parsed = resultService.calculateAll(link);

        // 2. sync турнира
        Tournament tournament = tournamentSyncService.sync(parsed, link);

        // ❗ если уже обработан — вообще выходим
        if (tournament.isProcessed()) {
            return new TournamentLinkResult(parsed, true, false);
        }

        // 3. проверка
        boolean alreadyExists = tournamentResultService.exists(
                player,
                parsed.getTournamentId()
        );

        boolean found = false;

        // 4. сохраняем только если нет
        if (!alreadyExists) {
            found = tournamentResultService.processResults(
                    parsed.getResults(),
                    player,
                    parsed.getTournamentId(),
                    parsed.getNightBonus(),
                    parsed.isFinished()
            );
        }

        // 5. 💥 ГЛАВНОЕ — закрываем турнир
        if (parsed.isFinished()) {
            tournament.setProcessed(true);
        }

        return new TournamentLinkResult(parsed, alreadyExists, found);
    }
}