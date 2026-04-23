package com.example.parser.modules.tournament.extraction;

import com.example.parser.core.model.LeagueType;
import com.example.parser.core.model.Match;
import com.example.parser.core.parser.LeagueDetector;
import com.example.parser.core.stats.NightBonusService;
import com.example.parser.modules.tournament.domain.TournamentContext;
import com.example.parser.modules.tournament.domain.TournamentStatus;
import com.example.parser.modules.tournament.parser.MatchParser;
import com.example.parser.modules.tournament.parser.TournamentParser;
import com.example.parser.modules.tournament.parser.TournamentStatusParser;
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