package ru.pulsecore.app.modules.tournament.parser;

import ru.pulsecore.app.modules.shared.HtmlSelectors;
import ru.pulsecore.app.core.model.Match;
import ru.pulsecore.app.modules.tournament.domain.model.Score;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchParser {

    private final RowParser rowParser;


    public List<Match> parseMatches(Document doc) {
        List<Match> matches = new ArrayList<>();
        Elements rows = doc.select(HtmlSelectors.ROW);

        for (Element row : rows) {
            Match match = rowParser.parse(row);
            if (match != null) {
                matches.add(match);
            }

        }

        return matches;
    }





}