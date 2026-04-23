package com.example.parser.modules.tournament.calculation.strategy.removed.handler;

import com.example.parser.modules.tournament.calculation.MatchStage;
import com.example.parser.modules.tournament.calculation.strategy.removed.RemovedPlayerHandler;
import com.example.parser.modules.tournament.calculation.strategy.removed.RemovedStage;
import com.example.parser.modules.tournament.calculation.strategy.DefaultMatchCalculationStrategy;
import com.example.parser.modules.tournament.domain.MatchProcessingResult;
import com.example.parser.modules.tournament.domain.TournamentContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class RemovedInGroupHandler implements RemovedPlayerHandler {

    private final DefaultMatchCalculationStrategy defaultStrategy;

    @Override
    public RemovedStage getStage() {
        return RemovedStage.GROUP;
    }

    @Override
    public MatchProcessingResult handle(TournamentContext ctx) {
        System.out.println("RemovedInGroupHandler СРАБОТАЛ");

        String removed = normalize(ctx.getRemovedPlayer());

        MatchProcessingResult result = defaultStrategy.process(ctx);

        Set<String> finalists = extractFinalists(ctx);

        // снявшийся → 4 место
        result.getPlaceMap().put(removed, 4);

        // оставшийся → 3 место
        findThirdPlaceCandidate(ctx, removed, finalists)
                .ifPresent(p -> result.getPlaceMap().put(p, 3));

        return result;
    }

    // =========================
    // 🔥 BUSINESS METHODS
    // =========================

    private Set<String> extractFinalists(TournamentContext ctx) {
        Set<String> finalists = new HashSet<>();

        ctx.getMatches().stream()
                .filter(m -> MatchStage.FINAL.matches(m.getStage()))
                .findFirst()
                .ifPresent(finalMatch -> {
                    finalists.add(normalize(finalMatch.getPlayer1()));
                    finalists.add(normalize(finalMatch.getPlayer2()));
                });

        return finalists;
    }

    private java.util.Optional<String> findThirdPlaceCandidate(
            TournamentContext ctx,
            String removed,
            Set<String> finalists
    ) {
        return ctx.getMatches().stream()
                .flatMap(m -> Stream.of(m.getPlayer1(), m.getPlayer2()))
                .map(this::normalize)
                .distinct()
                .filter(p -> !p.equals(removed))
                .filter(p -> !finalists.contains(p))
                .findFirst();
    }

    // =========================
    // 🔧 UTILS
    // =========================

    private String normalize(String name) {
        return name == null ? "" : name.toLowerCase().trim();
    }
}