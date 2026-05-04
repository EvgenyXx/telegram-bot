package ru.pulsecore.app.modules.tournament.calculation.strategy.removed;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RemovedHandlerRegistry {

    private final Map<RemovedStage, RemovedPlayerHandler> handlerMap;

    public RemovedHandlerRegistry(List<RemovedPlayerHandler> handlers) {
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(
                        RemovedPlayerHandler::getStage,
                        h -> h
                ));
    }

    public RemovedPlayerHandler get(RemovedStage stage) {
        return handlerMap.get(stage);
    }
}