package com.example.parser.parser;

import com.example.parser.config.HtmlSelectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Service
public class TournamentParser {

    public Long parseTournamentId(Document doc) {
        Element shortLink = doc.select(HtmlSelectors.SHORTLINK).first();

        if (shortLink == null) return null;

        String url = shortLink.attr("href");
        return Long.parseLong(url.replaceAll(".*p=(\\d+)", "$1"));
    }

    public boolean isFinished(Document doc) {
        Elements rows = doc.select(HtmlSelectors.ROW);

        for (Element row : rows) {
            Elements cols = row.select(HtmlSelectors.COL);
            if (cols.isEmpty()) continue;

            String stage = cols.get(0).text().trim().toLowerCase();

            if (stage.equals("финал")) {
                return row.selectFirst(".ml_tour_game_status.completed") != null;
            }
        }

        return false;
    }

    public String parseDate(Document doc) {
        Element dateElement = doc.select(HtmlSelectors.DATE).first();
        return dateElement != null ? dateElement.text() : null;
    }

    public String parseLeague(Document document){
        return document.select(HtmlSelectors.LEAGUE)
                .text()
                .trim();
    }

    public String parseTable(Document document){
        return document.select(HtmlSelectors.TABLE)
                .text()
                .replace("№", "")
                .trim();
    }
}