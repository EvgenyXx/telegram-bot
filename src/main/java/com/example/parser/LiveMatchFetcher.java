package com.example.parser;

import com.example.parser.domain.dto.LiveMatchData;
import com.example.parser.domain.model.Match;
import com.example.parser.parser.MatchParser;
import com.example.parser.parser.TournamentParser;
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

        return new LiveMatchData(live, finished,last);
    }
}