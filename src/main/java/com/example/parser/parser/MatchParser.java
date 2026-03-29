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

    private boolean isTournamentFinished(Document doc) {
        Elements statuses = doc.select(".ml_tour_game_status.completed");

        for (Element status : statuses) {
            Element row = status.closest(".ml_tour_game_list_row");
            if (row != null && row.text().toLowerCase().contains("финал")) {
                return true;
            }
        }

        return false;
    }
}