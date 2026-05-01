package com.example.parser.modules.tournament.application;

import com.example.parser.core.dto.ResultDto;
import com.example.parser.core.integration.DocumentLoader;
import com.example.parser.modules.tournament.calculation.strategy.MatchCalculationStrategy;
import com.example.parser.modules.tournament.calculation.strategy.StrategyResolver;
import com.example.parser.modules.tournament.calculation.ResultBuilder;
import com.example.parser.modules.tournament.domain.MatchProcessingResult;
import com.example.parser.modules.tournament.domain.ParsedResult;
import com.example.parser.modules.tournament.domain.TournamentContext;
import com.example.parser.modules.tournament.extraction.TournamentExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResultService {

    private final DocumentLoader loader;
    private final TournamentExtractor tournamentExtractor;
    private final StrategyResolver strategyResolver;
    private final ResultBuilder resultBuilder;

    public ParsedResult calculateAll(String url) throws Exception {
        Document doc = loader.load(url);
        return calculate(doc);
    }

    public ParsedResult calculateAll(Document doc) throws Exception {
        return calculate(doc);
    }

    private ParsedResult calculate(Document doc) throws Exception {
        TournamentContext ctx = tournamentExtractor.extract(doc);
        MatchCalculationStrategy strategy = strategyResolver.resolve(ctx);
        MatchProcessingResult matchResult = strategy.process(ctx);
        List<ResultDto> results = resultBuilder.build(matchResult, ctx);
        results.sort((a, b) -> Integer.compare(b.getTotal(), a.getTotal()));

        // Единственный информативный лог — результат обработки
        if (results.isEmpty() && ctx.getTournamentStatus() != null) {
            log.info("Tournament {}: no results (status={})",
                    ctx.getTournamentId(), ctx.getTournamentStatus());
        } else {
            log.info("Tournament {}: {} results, status={}",
                    ctx.getTournamentId(), results.size(), ctx.getTournamentStatus());
        }

        return new ParsedResult(
                ctx.getTournamentId(),
                results,
                ctx.getTournamentStatus(),
                ctx.getNightBonus()
        );
    }
}