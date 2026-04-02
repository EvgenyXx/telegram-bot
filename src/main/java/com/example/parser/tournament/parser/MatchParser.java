package com.example.parser.tournament.parser;

import com.example.parser.config.HtmlSelectors;
import com.example.parser.domain.model.Match;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MatchParser {

    public List<Match> parseMatches(Document doc) {
        List<Match> matches = new ArrayList<>();

        Elements rows = doc.select(HtmlSelectors.ROW);

        for (Element row : rows) {
            Elements cols = row.select(HtmlSelectors.COL);

            if (cols.size() <= HtmlSelectors.COL_SCORE) continue;

            String stage = cols.get(HtmlSelectors.COL_STAGE).text();
            String player1 = cols.get(HtmlSelectors.COL_PLAYER1).text();
            String scoreText = cols.get(HtmlSelectors.COL_SCORE).text();
            String player2 = cols.get(HtmlSelectors.COL_PLAYER2).text();

            int[] score = parseScore(scoreText);
            if (score == null) continue;

            Match match = new Match();
            match.setStage(stage);
            match.setPlayer1(player1);
            match.setPlayer2(player2);
            match.setScore1(score[0]);
            match.setScore2(score[1]);

            matches.add(match);
        }

        return matches;
    }

    public Match findLiveMatch(Document doc, String league, String table) {

        Elements rows = doc.select(HtmlSelectors.ROW);

        for (Element row : rows) {
            Element status = row.selectFirst(HtmlSelectors.STATUS);

            if (status != null && status.hasClass(HtmlSelectors.STATUS_GOES_CLASS)) {

                Elements players = row.select(HtmlSelectors.PLAYER);
                if (players.size() < 2) continue;

                Elements cols = row.select(HtmlSelectors.COL);
                if (cols.size() <= HtmlSelectors.COL_SCORE) continue;

                String fullScore = cols.get(HtmlSelectors.COL_SCORE).text();

                int[] score = parseScore(fullScore);
                if (score == null) continue;

                Match match = new Match();
                match.setStage(cols.get(HtmlSelectors.COL_STAGE).text());
                match.setPlayer1(players.get(0).text());
                match.setPlayer2(players.get(1).text());
                match.setScore1(score[0]);
                match.setScore2(score[1]);
                match.setSetsDetails(extractSets(fullScore));
                match.setLeague(league);
                match.setTable(table);

                return match;
            }
        }

        return null;
    }

    private int[] parseScore(String scoreText) {
        if (scoreText == null || !scoreText.contains(":")) return null;

        if (scoreText.contains("(")) {
            scoreText = scoreText.split("\\(")[0].trim();
        }

        String[] parts = scoreText.split(":");
        if (parts.length < 2) return null;

        try {
            return new int[]{
                    Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1].trim())
            };
        } catch (Exception e) {
            return null;
        }
    }

    private String extractSets(String fullScore) {
        if (fullScore != null && fullScore.contains("(")) {
            return fullScore.substring(fullScore.indexOf("(")).trim();
        }
        return "";
    }

    public Match findLastMatch(Document doc, String league, String table) {

        Elements rows = doc.select(".ml_tour_game_list_row");

        Element lastCompleted = null;

        for (Element row : rows) {
            Element status = row.selectFirst(".ml_tour_game_status");

            if (status != null && status.hasClass("completed")) {
                lastCompleted = row; // перезаписываем → в итоге будет последний
            }
        }

        if (lastCompleted == null) {
            return null;
        }

        return parseMatch(lastCompleted);
    }

    private Match parseMatch(Element row) {

        Elements players = row.select(".ml_tour_game_plr");
        if (players.size() < 2) return null;

        String player1 = players.get(0).text();
        String player2 = players.get(1).text();

        String score = row.select(".ml_game_res_points").text();
        if (!score.contains(":")) return null;

        String sets = row.select(".ml_game_res_sets").text();

        String[] scores = score.split(":");

        Match match = new Match();
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setScore1(Integer.parseInt(scores[0].trim()));
        match.setScore2(Integer.parseInt(scores[1].trim()));
        match.setSetsDetails(sets);

        return match;
    }


}