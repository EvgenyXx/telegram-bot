package com.example.parser.modules.tournament.parser;

import com.example.parser.modules.shared.HtmlSelectors;
import com.example.parser.modules.tournament.domain.TournamentStatus;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class TournamentStatusParser {

    public TournamentStatus parseStatus(Document doc) {
        if (isCancelled(doc)) {
            return TournamentStatus.CANCELLED;
        }
        if (isFinished(doc)) {
            return TournamentStatus.FINISHED;
        }
        if (isTournamentStarted(doc)) {
            return TournamentStatus.IN_PROGRESS;
        }
        return TournamentStatus.NOT_STARTED;
    }

    // =========================
    // 🔥 ЛОГИКА СТАТУСОВ
    // =========================

    private boolean isFinished(Document doc) {
        for (Element row : doc.select(HtmlSelectors.ROW)) {

            String stage = extractStage(row);
            if (stage == null) continue;

            if (isFinalStage(stage)) {
                return isCompleted(row);
            }
        }
        return false;
    }

    private boolean isTournamentStarted(Document doc) {
        for (Element row : doc.select(HtmlSelectors.ROW)) {

            if (isHeaderRow(row)) continue;

            Element status = row.selectFirst(HtmlSelectors.STATUS);
            if (status == null) continue;

            if (isActiveStatus(status)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCancelled(Document doc) {
        var statuses = doc.select(HtmlSelectors.STATUS);

        if (statuses.isEmpty()) return false;

        var realStatuses = statuses.stream()
                .filter(el -> !isHeaderText(el))
                .toList();

        if (realStatuses.isEmpty()) return false;

        return realStatuses.stream().allMatch(this::isCancelledStatus);
    }

    // =========================
    // 🧠 ЧИТАЕМЫЕ МЕТОДЫ
    // =========================

    private String extractStage(Element row) {
        Element stageEl = row.selectFirst(HtmlSelectors.STAGE);
        if (stageEl == null) return null;

        return stageEl.text()
                .trim()
                .toLowerCase();
    }

    private boolean isFinalStage(String stage) {
        return TournamentConstants.FINAL_STAGE.equals(stage);
    }

    private boolean isCompleted(Element row) {
        return row.selectFirst(HtmlSelectors.STATUS_COMPLETED_SELECTOR) != null;
    }

    private boolean isHeaderRow(Element row) {
        return row.text().contains(TournamentConstants.STATUS_HEADER);
    }

    private boolean isHeaderText(Element el) {
        return el.text().equalsIgnoreCase(TournamentConstants.STATUS_HEADER);
    }

    private boolean isActiveStatus(Element status) {
        String classes = status.className();

        return classes.contains(HtmlSelectors.STATUS_GOES_CLASS)
                || classes.contains(HtmlSelectors.STATUS_COMPLETED_CLASS);
    }

    private boolean isCancelledStatus(Element el) {
        return el.className().contains(TournamentConstants.STATUS_CANCELLED_EN)
                || el.text().toLowerCase().contains(TournamentConstants.STATUS_CANCELLED_RU);
    }
}