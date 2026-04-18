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

        // 🔥 1. уже отслеживается системой
        if (existing != null) {
            return new TournamentLinkResult(
                    TournamentLinkStatus.ALREADY_TRACKED,
                    null
            );
        }

        // 2. парсинг
        ResultService.ParsedResult parsed = resultService.calculateAll(link);

        // 3. sync турнира
        Tournament tournament = tournamentSyncService.sync(parsed, link);

        // ❗ уже обработан
        if (tournament.isProcessed()) {
            return new TournamentLinkResult(
                    TournamentLinkStatus.ALREADY_TRACKED,
                    parsed
            );
        }

        // 4. проверка — есть ли у пользователя
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

        // 5. пробуем сохранить
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

        // 6. завершён
        if (parsed.isFinished()) {
            tournament.setProcessed(true);

            return new TournamentLinkResult(
                    TournamentLinkStatus.FINISHED,
                    parsed
            );
        }

        // 7. начали отслеживание
        return new TournamentLinkResult(
                TournamentLinkStatus.TRACKING_STARTED,
                parsed
        );
    }


}