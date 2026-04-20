package com.example.parser.modules.tournament.parser;

import com.example.parser.modules.shared.HtmlSelectors;
import com.example.parser.core.model.Match;
import com.example.parser.modules.tournament.model.Score;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchParser {

    private final RowParser rowParser;
    private final ScoreParser scoreParser;
    private final MatchBuilder matchBuilder;

    public List<Match> parseMatches(Document doc) {
        List<Match> matches = new ArrayList<>();
        Elements rows = doc.select(HtmlSelectors.ROW);

        for (Element row : rows) {
            Match match = rowParser.parse(row);
            if (match != null) {
                matches.add(match);
            }
        }

        return matches;
    }

    public Match findLiveMatch(Document doc, String league, String table) {
        Elements rows = doc.select(HtmlSelectors.ROW);

        for (Element row : rows) {
            Element status = row.selectFirst(HtmlSelectors.STATUS);

            if (status != null && status.hasClass(HtmlSelectors.STATUS_GOES_CLASS)) {
                Elements players = row.select(HtmlSelectors.PLAYER);

                log.warn("PLAYERS SIZE = {}", players.size());

                if (players.size() < 2) {
                    continue;
                }

                Elements cols = row.select(HtmlSelectors.COL);

                if (cols.size() <= HtmlSelectors.COL_SCORE) {
                    continue;
                }

                String fullScore = cols.get(HtmlSelectors.COL_SCORE).text();

                Score score = scoreParser.parseScore(fullScore);

                if (score == null) {
                    continue;
                }

                return matchBuilder.build(
                        cols.get(HtmlSelectors.COL_STAGE).text(),
                        players.get(0).text(),
                        players.get(1).text(),
                        score,
                        scoreParser.extractSets(fullScore),
                        league,
                        table
                );
            }
        }

        return null;
    }

    public Match findLastMatch(Document doc, String league, String table) {
        Elements rows = doc.select(HtmlSelectors.ROW_ALT);

        Element lastCompleted = null;

        for (Element row : rows) {
            Element status = row.selectFirst(HtmlSelectors.STATUS_ALT);

            if (status != null && status.hasClass(HtmlSelectors.STATUS_COMPLETED_CLASS)) {
                lastCompleted = row;
            }
        }

        if (lastCompleted == null) {
            return null;
        }

        return parseMatch(lastCompleted, league, table);
    }

    private Match parseMatch(Element row, String league, String table) {
        Elements players = row.select(HtmlSelectors.PLAYER_ALT);

        if (players.size() < 2) {
            return null;
        }

        String player1 = players.get(0).text();
        String player2 = players.get(1).text();

        String scoreText = row.select(HtmlSelectors.SCORE_ALT).text();
        String sets = row.select(HtmlSelectors.SETS_ALT).text();

        log.warn("RAW → p1={}, p2={}, score={}, sets={}",
                player1, player2, scoreText, sets);

        Score score = scoreParser.parseScore(scoreText);

        if (score == null) {
            log.warn("SKIP → invalid score");
            return null;
        }

        return matchBuilder.build(
                "",
                player1,
                player2,
                score,
                sets,
                league,
                table
        );
    }
}