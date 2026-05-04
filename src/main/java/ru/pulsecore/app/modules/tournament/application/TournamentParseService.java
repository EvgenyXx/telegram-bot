package ru.pulsecore.app.modules.tournament.application;

import ru.pulsecore.app.core.integration.DocumentLoader;
import ru.pulsecore.app.core.model.Match;
import ru.pulsecore.app.modules.tournament.api.dto.ParsedTournament;
import ru.pulsecore.app.modules.tournament.parser.MatchParser;
import ru.pulsecore.app.modules.tournament.parser.TournamentParser;
import ru.pulsecore.app.modules.tournament.parser.TournamentStatusParser;
import ru.pulsecore.app.modules.tournament.domain.TournamentStatus;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentParseService {

    private final DocumentLoader loader;
    private final MatchParser matchParser;
    private final TournamentParser tournamentParser;
    private final TournamentStatusParser tournamentStatusParser;

    public ParsedTournament parse(String url) throws Exception {
        Document doc = loader.load(url);

        Long id = tournamentParser.parseTournamentId(doc);
        TournamentStatus status = tournamentStatusParser.parseStatus(doc);
        List<Match> matches = matchParser.parseMatches(doc);

        return new ParsedTournament(id, matches, status);
    }




}