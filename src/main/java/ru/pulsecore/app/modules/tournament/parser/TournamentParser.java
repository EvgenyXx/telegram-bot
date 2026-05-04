package ru.pulsecore.app.modules.tournament.parser;

import ru.pulsecore.app.modules.shared.HtmlSelectors;
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

    // 🔥 ГЛАВНОЕ ИСПРАВЛЕНИЕ
    public String findRemovedPlayer(Document doc) {
        return doc.select(HtmlSelectors.PLAYER)
                .stream()
                .filter(player -> player.hasClass(HtmlSelectors.STATUS_REMOVED))
                .map(Element::text)
                .map(this::normalize) // 👈 КЛЮЧЕВОЕ
                .findFirst()
                .orElse(null);
    }

    // 🔥 ТАКОЙ ЖЕ normalize как в стратегии
    private String normalize(String name) {
        if (name == null) return "";

        return name.toLowerCase()
                .replace("\u00A0", " ")
                .replaceAll("\\(.*?\\)", "") // 👈 убираем "(снят)" и т.п.
                .replaceAll("\\s+", " ")
                .trim();
    }
}