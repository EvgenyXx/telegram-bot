package ru.pulsecore.app.modules.tournament.extraction;

import ru.pulsecore.app.modules.tournament.calculation.strategy.removed.RemovedStage;

public record RemovedResult(
        String player,
        RemovedStage stage
) {}