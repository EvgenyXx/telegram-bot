package com.example.parser.modules.tournament.parser;

import com.example.parser.modules.shared.HtmlSelectors;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class TournamentParser {

    public Long parseTournamentId(Document doc) {
        Element shortLink = doc.select(HtmlSelectors.SHORTLINK).first();

        if (shortLink == null) return null;

        String url = shortLink.attr("href");

        return Long.parseLong(url.replaceAll(".*p=(\\d+)", "$1"));
    }

    public String parseDate(Document doc) {
        Element dateElement = doc.select(HtmlSelectors.DATE).first();
        return dateElement != null ? dateElement.text() : null;
    }

    public String parseLeague(Document document) {
        return document.select(HtmlSelectors.LEAGUE)
                .text()
                .trim();
    }

    public String parseTable(Document document) {
        return document.select(HtmlSelectors.TABLE)
                .text()
                .replace("№", "")
                .trim();
    }

    public boolean hasAnyRemovedPlayer(Document doc) {
        return doc.select(HtmlSelectors.PLAYER)
                .stream()
                .anyMatch(player -> player.hasClass(HtmlSelectors.STATUS_REMOVED));
    }
}