package com.example.parser.tournament;


import com.example.parser.domain.dto.TournamentLinkResult;
import com.example.parser.domain.entity.Tournament;
import com.example.parser.player.Player;
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

        // 3. проверка
        boolean alreadyExists = tournamentResultService.exists(
                player,
                parsed.getTournamentId()
        );

        // 4. сохранение результатов
        boolean found = tournamentResultService.processResults(
                parsed.getResults(),
                player,
                parsed.getTournamentId(),
                parsed.getNightBonus(),
                parsed.isFinished()
        );

        return new TournamentLinkResult(parsed, alreadyExists, found);
    }
}