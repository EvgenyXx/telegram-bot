package com.example.parser.tournament.parser;

import com.example.parser.config.HtmlSelectors;
import com.example.parser.domain.model.Match;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MatchParser {

    public List<Match> parseMatches(Document doc) {

        log.warn("════════ MATCH PARSE START ════════");
        log.warn("DOC TITLE = {}", doc.title());
        log.warn("HTML LENGTH = {}", doc.html().length());

        List<Match> matches = new ArrayList<>();

        Elements rows = doc.select(HtmlSelectors.ROW);

        log.warn("ROWS FOUND = {}", rows.size());
        log.warn("SELECTOR ROW = {}", HtmlSelectors.ROW);

        int index = 0;

        for (Element row : rows) {

            log.warn("---- ROW {} ----", index++);
            log.warn("ROW HTML = {}", row.outerHtml());

            Elements cols = row.select(HtmlSelectors.COL);

            log.warn("COLS SIZE = {}", cols.size());
            log.warn("SELECTOR COL = {}", HtmlSelectors.COL);

            if (cols.size() <= HtmlSelectors.COL_SCORE) {
                log.warn("SKIP → not enough cols");
                continue;
            }

            String stage = cols.get(HtmlSelectors.COL_STAGE).text();
            String player1 = cols.get(HtmlSelectors.COL_PLAYER1).text();
            String scoreText = cols.get(HtmlSelectors.COL_SCORE).text();
            String player2 = cols.get(HtmlSelectors.COL_PLAYER2).text();

            log.warn("PARSED RAW → stage={}, p1={}, score={}, p2={}",
                    stage, player1, scoreText, player2);

            int[] score = parseScore(scoreText);

            if (score == null) {
                log.warn("SKIP → score parse failed");
                continue;
            }

            Match match = new Match();
            match.setStage(stage);
            match.setPlayer1(player1);
            match.setPlayer2(player2);
            match.setScore1(score[0]);
            match.setScore2(score[1]);

            log.warn("MATCH OK → {} {}:{} {}",
                    player1, score[0], score[1], player2);

            matches.add(match);
        }

        log.warn("PARSED MATCHES SIZE = {}", matches.size());
        log.warn("════════ MATCH PARSE END ════════");

        return matches;
    }

    public Match findLiveMatch(Document doc, String league, String table) {

        log.warn("════════ FIND LIVE MATCH START ════════");

        Elements rows = doc.select(HtmlSelectors.ROW);
        log.warn("ROWS FOUND = {}", rows.size());

        for (Element row : rows) {

            log.warn("ROW HTML = {}", row.outerHtml());

            Element status = row.selectFirst(HtmlSelectors.STATUS);

            if (status != null) {
                log.warn("STATUS CLASS = {}", status.className());
            }

            if (status != null && status.hasClass(HtmlSelectors.STATUS_GOES_CLASS)) {

                log.warn("LIVE MATCH FOUND");

                Elements players = row.select(HtmlSelectors.PLAYER);
                log.warn("PLAYERS SIZE = {}", players.size());

                if (players.size() < 2) {
                    log.warn("SKIP → not enough players");
                    continue;
                }

                Elements cols = row.select(HtmlSelectors.COL);
                log.warn("COLS SIZE = {}", cols.size());

                if (cols.size() <= HtmlSelectors.COL_SCORE) {
                    log.warn("SKIP → not enough cols");
                    continue;
                }

                String fullScore = cols.get(HtmlSelectors.COL_SCORE).text();

                log.warn("FULL SCORE = {}", fullScore);

                int[] score = parseScore(fullScore);

                if (score == null) {
                    log.warn("SKIP → score parse failed");
                    continue;
                }

                Match match = new Match();
                match.setStage(cols.get(HtmlSelectors.COL_STAGE).text());
                match.setPlayer1(players.get(0).text());
                match.setPlayer2(players.get(1).text());
                match.setScore1(score[0]);
                match.setScore2(score[1]);
                match.setSetsDetails(extractSets(fullScore));
                match.setLeague(league);
                match.setTable(table);

                log.warn("LIVE MATCH OK → {} {}:{} {}",
                        match.getPlayer1(), score[0], score[1], match.getPlayer2());

                return match;
            }
        }

        log.warn("NO LIVE MATCH FOUND");
        log.warn("════════ FIND LIVE MATCH END ════════");

        return null;
    }

    private int[] parseScore(String scoreText) {

        log.warn("PARSE SCORE → raw={}", scoreText);

        if (scoreText == null || !scoreText.contains(":")) {
            log.warn("PARSE SCORE FAIL → no ':'");
            return null;
        }

        if (scoreText.contains("(")) {
            scoreText = scoreText.split("\\(")[0].trim();
            log.warn("PARSE SCORE → cleaned={}", scoreText);
        }

        String[] parts = scoreText.split(":");

        if (parts.length < 2) {
            log.warn("PARSE SCORE FAIL → parts < 2");
            return null;
        }

        try {
            int s1 = Integer.parseInt(parts[0].trim());
            int s2 = Integer.parseInt(parts[1].trim());

            log.warn("PARSE SCORE OK → {}:{}", s1, s2);

            return new int[]{s1, s2};

        } catch (Exception e) {
            log.error("❌ PARSE SCORE ERROR", e);
            return null;
        }
    }

    private String extractSets(String fullScore) {

        log.warn("EXTRACT SETS → {}", fullScore);

        if (fullScore != null && fullScore.contains("(")) {
            String sets = fullScore.substring(fullScore.indexOf("(")).trim();
            log.warn("SETS FOUND → {}", sets);
            return sets;
        }

        log.warn("NO SETS");
        return "";
    }

    public Match findLastMatch(Document doc, String league, String table) {

        log.warn("════════ FIND LAST MATCH START ════════");

        Elements rows = doc.select(".ml_tour_game_list_row");
        log.warn("ROWS FOUND = {}", rows.size());

        Element lastCompleted = null;

        for (Element row : rows) {

            Element status = row.selectFirst(".ml_tour_game_status");

            if (status != null) {
                log.warn("STATUS CLASS = {}", status.className());
            }

            if (status != null && status.hasClass("completed")) {
                log.warn("FOUND COMPLETED MATCH");
                lastCompleted = row;
            }
        }

        if (lastCompleted == null) {
            log.warn("NO COMPLETED MATCH FOUND");
            return null;
        }

        return parseMatch(lastCompleted);
    }

    private Match parseMatch(Element row) {

        log.warn("PARSE LAST MATCH → HTML = {}", row.outerHtml());

        Elements players = row.select(".ml_tour_game_plr");

        log.warn("PLAYERS SIZE = {}", players.size());

        if (players.size() < 2) {
            log.warn("SKIP → not enough players");
            return null;
        }

        String player1 = players.get(0).text();
        String player2 = players.get(1).text();

        String score = row.select(".ml_game_res_points").text();
        String sets = row.select(".ml_game_res_sets").text();

        log.warn("RAW → p1={}, p2={}, score={}, sets={}",
                player1, player2, score, sets);

        if (!score.contains(":")) {
            log.warn("SKIP → invalid score");
            return null;
        }

        String[] scores = score.split(":");

        Match match = new Match();
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setScore1(Integer.parseInt(scores[0].trim()));
        match.setScore2(Integer.parseInt(scores[1].trim()));
        match.setSetsDetails(sets);

        log.warn("LAST MATCH OK → {} {}:{} {}",
                player1,
                match.getScore1(),
                match.getScore2(),
                player2
        );

        return match;
    }
}