package com.example.parser.modules.tournament.service;


import com.example.parser.core.dto.TournamentLinkResult;
import com.example.parser.modules.tournament.domain.Tournament;
import com.example.parser.modules.player.domain.Player;
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



        // 1. парсинг
        ResultService.ParsedResult parsed = resultService.calculateAll(link);

        if (existing != null) {
            if (existing.isStarted() && !existing.isFinished()) {
                return new TournamentLinkResult(parsed, true, false);
            }
        }

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