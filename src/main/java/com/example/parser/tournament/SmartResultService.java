package com.example.parser.tournament;

import com.example.parser.domain.model.Match;
import com.example.parser.integration.DocumentLoader;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SmartResultService {

    private final ResultService resultService;
    private final List<TournamentStrategy> strategies;
    private final DocumentLoader loader;

    public ResultService.ParsedResult calculate(String link) throws Exception {

        Document doc = loader.load(link);
        List<Match> matches = resultService.getMatches(doc);

        // 👉 пробуем стратегии
        for (TournamentStrategy strategy : strategies) {
            if (strategy.isApplicable(doc, matches)) {
                return strategy.calculate(doc, matches);
            }
        }

        // 👉 fallback — старый код
        return resultService.calculateAll(doc);
    }
}