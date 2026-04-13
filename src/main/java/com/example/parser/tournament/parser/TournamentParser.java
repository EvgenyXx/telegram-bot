package com.example.parser.tournament.parser;

import com.example.parser.config.HtmlSelectors;
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

    public static boolean isTournamentStarted(Document doc) {
        var matches = doc.select(".ml_tour_game_list_row");

        if (matches.isEmpty()) {
            return false;
        }

        // 👉 берём первый реальный матч (пропускаем header)
        Element firstMatch = matches.stream()
                .filter(m -> !m.text().contains("Статус"))
                .findFirst()
                .orElse(null);

        if (firstMatch == null) {
            return false;
        }

        // 👉 достаём статус
        Element status = firstMatch.selectFirst(".ml_tour_game_status");

        if (status == null) {
            return false;
        }

        // 👉 проверяем класс статуса
        String classes = status.className();

        // true только если матч реально идёт
        return classes.contains("goes");
    }

    public boolean hasAnyRemovedPlayer(Document doc){
        for (Element player : doc.select(HtmlSelectors.PLAYER)) {
            if (player.hasClass(HtmlSelectors.STATUS_REMOVED)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCancelled(Document doc) {
        Elements statuses = doc.select(".ml_tour_game_status");

        if (statuses.isEmpty()) return false;

        // 🔥 убираем заголовок "Статус"
        var realStatuses = statuses.stream()
                .filter(el -> !el.text().equalsIgnoreCase("Статус"))
                .toList();

        if (realStatuses.isEmpty()) return false;

        // 🔥 если ВСЕ матчи отменены → турнир отменен
        return realStatuses.stream().allMatch(el ->
                el.className().contains("canceled") ||
                        el.text().toLowerCase().contains("отменен")
        );
    }
}