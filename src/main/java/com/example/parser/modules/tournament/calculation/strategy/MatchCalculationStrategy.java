package com.example.parser.modules.tournament.calculation.strategy;

import com.example.parser.modules.tournament.domain.MatchProcessingResult;
import com.example.parser.modules.tournament.domain.TournamentContext;

public interface MatchCalculationStrategy {

    StrategyType getType() ;

    MatchProcessingResult process(TournamentContext ctx);
}