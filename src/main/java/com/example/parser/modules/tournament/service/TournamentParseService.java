package com.example.parser.modules.tournament.service;

import com.example.parser.core.integration.DocumentLoader;
import com.example.parser.core.model.Match;
import com.example.parser.modules.tournament.dto.ParsedTournament;
import com.example.parser.modules.tournament.parser.MatchParser;
import com.example.parser.modules.tournament.parser.TournamentParser;
import com.example.parser.modules.tournament.service.result.TournamentStatus;
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

    public ParsedTournament parse(String url) throws Exception {
        Document doc = loader.load(url);

        Long id = tournamentParser.parseTournamentId(doc);
        TournamentStatus status = tournamentParser.parseStatus(doc);
        List<Match> matches = matchParser.parseMatches(doc);

        return new ParsedTournament(id, matches, status);
    }




}