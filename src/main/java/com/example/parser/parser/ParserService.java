package com.example.parser.parser;

import com.example.parser.integration.DocumentLoader;
import com.example.parser.domain.model.Match;
import com.example.parser.domain.model.ParsedTournament;
import com.example.parser.tournament.parser.MatchParser;
import com.example.parser.tournament.parser.TournamentParser;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParserService {

    private final DocumentLoader loader;
    private final MatchParser matchParser;
    private final TournamentParser tournamentParser;

    public ParsedTournament parse(String url) throws Exception {
        Document doc = loader.load(url);

        Long id = tournamentParser.parseTournamentId(doc);
        boolean finished = tournamentParser.isFinished(doc);
        List<Match> matches = matchParser.parseMatches(doc);

        return new ParsedTournament(id, matches, finished);
    }

//    public boolean isTournamentStarted(String url) throws Exception {
//        Document doc = loader.load(url);
//        return tournamentParser.isTournamentStarted(doc);
//    }

    public boolean isFinished(String url) throws Exception {
        Document document = loader.load(url);
        return tournamentParser.isFinished(document);
    }
}