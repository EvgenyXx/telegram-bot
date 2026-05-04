package ru.pulsecore.app.modules.tournament.extraction;

import ru.pulsecore.app.core.model.LeagueType;
import ru.pulsecore.app.core.model.Match;
import ru.pulsecore.app.core.parser.LeagueDetector;
import ru.pulsecore.app.core.stats.NightBonusService;
import ru.pulsecore.app.modules.tournament.domain.TournamentContext;
import ru.pulsecore.app.modules.tournament.domain.TournamentStatus;
import ru.pulsecore.app.modules.tournament.parser.MatchParser;
import ru.pulsecore.app.modules.tournament.parser.TournamentParser;
import ru.pulsecore.app.modules.tournament.parser.TournamentStatusParser;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TournamentExtractor {

    private final TournamentParser tournamentParser;
    private final MatchParser matchParser;
    private final LeagueDetector leagueDetector;
    private final NightBonusService nightBonusService;
    private final TournamentStatusParser tournamentStatusParser;
    private final RemovedPlayerDetector removedPlayerDetector;

    public TournamentContext extract(Document doc) throws Exception {

        Long tournamentId = tournamentParser.parseTournamentId(doc);
        TournamentStatus status = tournamentStatusParser.parseStatus(doc);
        String date = tournamentParser.parseDate(doc);

        List<Match> matches = matchParser.parseMatches(doc);

        LeagueType league = leagueDetector.detectLeague(doc);
        double nightBonus = nightBonusService.calculateBonus(doc, league.name());

        // =========================
        // REMOVED LOGIC
        // =========================
        String removedPlayer = tournamentParser.findRemovedPlayer(doc);


        RemovedResult playerDetector = removedPlayerDetector.detect(removedPlayer,matches);



        return new TournamentContext(
                tournamentId,
                status,
                date,
                matches,
                league,
                nightBonus,
                playerDetector.stage(),
                playerDetector.player()
        );
    }


}