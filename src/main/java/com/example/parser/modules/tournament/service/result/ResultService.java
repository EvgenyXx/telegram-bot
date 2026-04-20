package com.example.parser.modules.tournament.service.result;

import com.example.parser.core.dto.ResultDto;

import com.example.parser.core.integration.DocumentLoader;
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
    private final MatchProcessor matchProcessor;
    private final ResultBuilder resultBuilder;

    public ParsedResult calculateAll(String url) throws Exception {
        log.info("▶ START parse: {}", url);
        Document doc = loader.load(url);
        return calculate(doc);
    }

    public ParsedResult calculateAll(Document doc) throws Exception {
        return calculate(doc);
    }

    private ParsedResult calculate(Document doc) throws Exception {

        TournamentContext ctx = tournamentExtractor.extract(doc);

        MatchProcessingResult matchResult =
                matchProcessor.process(ctx);

        List<ResultDto> results =
                resultBuilder.build(matchResult, ctx);

        results.sort((a, b) -> Integer.compare(b.getTotal(), a.getTotal()));

        log.info("✅ DONE tournamentId={} → results={}, finished={}",
                ctx.getTournamentId(),
                results.size(),
                ctx.getTournamentStatus());

        return new ParsedResult(
                ctx.getTournamentId(),
                results,
                ctx.getTournamentStatus(),
                ctx.getNightBonus(),
                ctx.isHasRemovedPlayers()
        );
    }
}