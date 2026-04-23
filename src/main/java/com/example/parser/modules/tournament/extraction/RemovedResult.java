package com.example.parser.modules.tournament.extraction;

import com.example.parser.modules.tournament.calculation.strategy.removed.RemovedStage;

public record RemovedResult(
        String player,
        RemovedStage stage
) {}