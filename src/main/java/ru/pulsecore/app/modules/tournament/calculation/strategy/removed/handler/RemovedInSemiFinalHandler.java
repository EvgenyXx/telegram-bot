package ru.pulsecore.app.modules.tournament.calculation.strategy.removed.handler;

import ru.pulsecore.app.core.model.Match;
import ru.pulsecore.app.modules.tournament.calculation.MatchStage;
import ru.pulsecore.app.modules.tournament.calculation.strategy.DefaultMatchCalculationStrategy;
import ru.pulsecore.app.modules.tournament.calculation.strategy.removed.RemovedPlayerHandler;
import ru.pulsecore.app.modules.tournament.calculation.strategy.removed.RemovedStage;
import ru.pulsecore.app.modules.tournament.domain.MatchProcessingResult;
import ru.pulsecore.app.modules.tournament.domain.TournamentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j//todo  чутка доделать
public class RemovedInSemiFinalHandler implements RemovedPlayerHandler {

    private final DefaultMatchCalculationStrategy defaultStrategy;

    @Override
    public RemovedStage getStage() {
        return RemovedStage.SEMI_FINAL;
    }

    @Override
    public MatchProcessingResult handle(TournamentContext ctx) {

        MatchProcessingResult result = defaultStrategy.process(ctx);

        var semis = ctx.getMatches().stream()
                .filter(m -> MatchStage.SEMI_FINAL.matches(m.getStage()))
                .toList();

        if (semis.size() < 2) {
            return result;
        }

        var finalMatch = ctx.getMatches().stream()
                .filter(m -> MatchStage.FINAL.matches(m.getStage()))
                .findFirst()
                .orElse(null);

        if (finalMatch == null) {
            return result;
        }

        String f1 = normalize(finalMatch.getPlayer1());
        String f2 = normalize(finalMatch.getPlayer2());

        // 🔥 1. находим отмененный полуфинал
        var canceledSemi = semis.stream()
                .filter(this::isCanceled)
                .findFirst()
                .orElse(null);

        if (canceledSemi == null) {
            return result;
        }

        String c1 = normalize(canceledSemi.getPlayer1());
        String c2 = normalize(canceledSemi.getPlayer2());

        // 🔥 2. кто НЕ в финале — тот снялся
        String removed = (c1.equals(f1) || c1.equals(f2)) ? c2 : c1;

        log.info("REMOVED (4 place): {}", removed);

        result.getPlaceMap().put(removed, 4);

        // 🔥 3. второй полуфинал → проигравший = 3 место
        semis.stream()
                .filter(m -> !isCanceled(m))
                .findFirst()
                .ifPresent(m -> {
                    String p1 = normalize(m.getPlayer1());
                    String p2 = normalize(m.getPlayer2());

                    String loser = m.getScore1() > m.getScore2() ? p2 : p1;

                    log.info("THIRD PLACE: {}", loser);

                    result.getPlaceMap().put(loser, 3);
                });

        return result;
    }

    // =========================

    private boolean isCanceled(Match m) {
        return m.getStatus() != null &&
                m.getStatus().toLowerCase().contains("отмен");
    }

    private String normalize(String name) {
        return name == null ? "" : name.toLowerCase().trim();
    }
}