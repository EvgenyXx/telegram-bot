package com.example.parser.modules.tournament.calculation.strategy.removed;

import com.example.parser.modules.tournament.calculation.strategy.StrategyType;
import com.example.parser.modules.tournament.calculation.strategy.DefaultMatchCalculationStrategy;
import com.example.parser.modules.tournament.calculation.strategy.MatchCalculationStrategy;
import com.example.parser.modules.tournament.domain.MatchProcessingResult;
import com.example.parser.modules.tournament.domain.TournamentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RemovedPlayerMatchCalculationStrategy implements MatchCalculationStrategy {

    private final DefaultMatchCalculationStrategy defaultStrategy;
    private final RemovedHandlerRegistry registry;

    @Override
    public StrategyType getType() {
        return StrategyType.REMOVED;
    }

    @Override
    public MatchProcessingResult process(TournamentContext ctx) {

        RemovedStage stage = ctx.getRemovedStage();

        if (stage == null || stage == RemovedStage.NONE) {
            if (log.isDebugEnabled()) {
                log.debug("Removed strategy → fallback to DEFAULT");
            }
            return defaultStrategy.process(ctx);
        }

        RemovedPlayerHandler handler = registry.get(stage);

        if (handler == null) {
            throw new IllegalStateException("No handler for stage: " + stage);
        }

        return handler.handle(ctx);
    }
}