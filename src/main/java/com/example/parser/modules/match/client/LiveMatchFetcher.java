package com.example.parser.modules.match.client;

import com.example.parser.core.dto.LiveMatchData;
import com.example.parser.core.model.Match;
import com.example.parser.modules.tournament.parser.MatchParser;
import com.example.parser.modules.tournament.parser.TournamentParser;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LiveMatchFetcher {

    private final MatchParser matchParser;
    private final TournamentParser tournamentParser;

    public LiveMatchData fetch(String link) throws Exception {

        Document doc = Jsoup.connect(link).get();

        String league = tournamentParser.parseLeague(doc);
        String table = tournamentParser.parseTable(doc);

        Match live = matchParser.findLiveMatch(doc, league, table);
        Match last = matchParser.findLastMatch(doc, league, table);

        boolean finished = tournamentParser.isFinished(doc);



        return new LiveMatchData(live, finished, last);
    }
}