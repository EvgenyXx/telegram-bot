package ru.pulsecore.app.modules.tournament.parser;

import ru.pulsecore.app.modules.shared.HtmlSelectors;
import ru.pulsecore.app.core.model.Match;
import ru.pulsecore.app.modules.tournament.domain.model.Score;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RowParser {

    private final ScoreParser scoreParser;
    private final MatchBuilder matchBuilder;

    public Match parse(Element row) {
        Elements cols = row.select(HtmlSelectors.COL);

        if (cols.size() <= HtmlSelectors.COL_SCORE) {
            return null;
        }

        String stage = cols.get(HtmlSelectors.COL_STAGE).text();
        String player1 = cols.get(HtmlSelectors.COL_PLAYER1).text();
        String scoreText = cols.get(HtmlSelectors.COL_SCORE).text();
        String player2 = cols.get(HtmlSelectors.COL_PLAYER2).text();
        String status = row.select(HtmlSelectors.STATUS).text();

        Score score = scoreParser.parseScore(scoreText);

        boolean isCancelled = status != null
                && status.toLowerCase().contains("отмен");

        // пропускаем только реально некорректные матчи
        if (score == null && !isCancelled) {
            return null;
        }

        return matchBuilder.build(
                stage,
                player1,
                player2,
                score, // может быть null для отменённых
                "",
                null,
                null,
                status
        );
    }
}