package com.example.parser.parser;

import com.example.parser.model.Match;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MatchParser {

    private Document lastDocument; // 🔥 добавили

    public static class ParsedTournament {
        private Long tournamentId;
        private List<Match> matches;
        private boolean finished;

        public ParsedTournament(Long tournamentId, List<Match> matches, boolean finished) {
            this.tournamentId = tournamentId;
            this.matches = matches;
            this.finished = finished;
        }

        public Long getTournamentId() {
            return tournamentId;
        }

        public List<Match> getMatches() {
            return matches;
        }

        public boolean isFinished() {
            return finished;
        }
    }

    public ParsedTournament parseMatches(String url) throws Exception {
        Document doc = Jsoup.connect(url).get();
        this.lastDocument = doc; // 🔥 сохраняем

        Long tournamentId = parseTournamentId(doc);
        boolean finished = isTournamentFinished(doc);

        List<Match> matches = new ArrayList<>();

        Elements rows = doc.select(".ml_tour_game_list_row");

        for (Element row : rows) {
            Elements cols = row.select(".ml_tour_game_list_col");
            if (cols.size() < 6) continue;

            String stage = cols.get(0).text();
            String player1 = cols.get(3).text();
            String scoreText = cols.get(4).text();
            String player2 = cols.get(5).text();

            if (!scoreText.contains(":")) continue;

            scoreText = scoreText.split("\\(")[0];
            String[] scoreParts = scoreText.split(":");

            int a = Integer.parseInt(scoreParts[0].trim());
            int b = Integer.parseInt(scoreParts[1].trim());

            Match match = new Match();
            match.setStage(stage);
            match.setPlayer1(player1);
            match.setPlayer2(player2);
            match.setScore1(a);
            match.setScore2(b);

            matches.add(match);
        }

        return new ParsedTournament(tournamentId, matches, finished);
    }

    public String parseDate(String url) throws Exception {
        Document doc = Jsoup.connect(url).get();
        Element dateElement = doc.select("table.info_table tr:contains(Дата:) td").first();
        return dateElement != null ? dateElement.text() : null;
    }

    public Document getLastDocument() { // 🔥 добавили
        return lastDocument;
    }

    private Long parseTournamentId(Document doc) {
        Element shortLink = doc.select("link[rel=shortlink]").first();
        if (shortLink == null) return null;

        String url = shortLink.attr("href");
        return Long.parseLong(url.replaceAll(".*p=(\\d+)", "$1"));
    }

    public boolean isTournamentFinished(Document doc) {
        Elements rows = doc.select(".ml_tour_game_list_row");

        for (Element row : rows) {
            Elements cols = row.select(".ml_tour_game_list_col");
            if (cols.isEmpty()) continue;

            String stage = cols.get(0).text().trim().toLowerCase();
            boolean isCompleted = row.select(".ml_tour_game_status.completed").size() > 0;

            if (stage.equals("финал") && isCompleted) {
                return true;
            }
        }

        return false;
    }


    public Match findLiveMatch(Document doc) {

        Elements rows = doc.select(".ml_tour_game_list_row");

        for (Element row : rows) {

            Element status = row.selectFirst(".ml_tour_game_status");

            if (status != null && status.hasClass("goes")) {

                Elements players = row.select(".ml_tour_game_plr");

                String player1 = players.get(0).text();
                String player2 = players.get(1).text();

                // 🔥 БЕРЕМ ВСЮ СТРОКУ С РЕЗУЛЬТАТОМ
                String fullScore = row.select(".ml_tour_game_list_col").get(4).text();

                // пример: 2:1 (10:12 11:5 11:3 5:4)

                String sets = "";
                String scoreMain = fullScore;

                if (fullScore.contains("(")) {
                    scoreMain = fullScore.split("\\(")[0].trim();
                    sets = fullScore.substring(fullScore.indexOf("(")).trim();
                }

                String[] parts = scoreMain.split(":");
                String stage = row.select(".ml_tour_game_list_col").get(0).text();

                Match match = new Match();
                match.setStage(stage);
                match.setPlayer1(player1);
                match.setPlayer2(player2);
                match.setScore1(Integer.parseInt(parts[0].trim()));
                match.setScore2(Integer.parseInt(parts[1].trim()));
                match.setSetsDetails(sets);

                return match;
            }
        }

        return null;
    }


}