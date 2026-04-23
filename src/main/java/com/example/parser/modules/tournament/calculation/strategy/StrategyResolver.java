package com.example.parser.modules.tournament.calculation.strategy;

import com.example.parser.modules.tournament.domain.TournamentContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class StrategyResolver {

    private final Map<StrategyType, MatchCalculationStrategy> strategyMap;

    public StrategyResolver(List<MatchCalculationStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        MatchCalculationStrategy::getType,
                        s -> s
                ));
    }

    public MatchCalculationStrategy resolve(TournamentContext ctx) {
        StrategyType type = resolveType(ctx);

        MatchCalculationStrategy strategy = strategyMap.get(type);

        if (strategy == null) {
            throw new IllegalStateException("No strategy found for type: " + type);
        }

        if (log.isDebugEnabled()) {
            log.debug("▶ Strategy resolved: type={}", type);
        }

        return strategy;
    }

    private StrategyType resolveType(TournamentContext ctx) {
        if (ctx.getRemovedPlayer() != null && !ctx.getRemovedPlayer().isBlank()) {
            return StrategyType.REMOVED;
        }
        return StrategyType.DEFAULT;
    }
}