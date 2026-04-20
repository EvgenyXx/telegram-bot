package com.example.parser.modules.tournament.service.result;

import com.example.parser.core.model.Match;
import com.example.parser.core.stats.PlacementCalculator;
import com.example.parser.core.stats.PointsCalculator;
import com.example.parser.core.stats.PointsCalculatorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MatchProcessor {

    private final PlacementCalculator placementCalculator;
    private final PointsCalculatorFactory factory;

    public MatchProcessingResult process(TournamentContext ctx) {

        PointsCalculator calculator =
                factory.getCalculator(ctx.getLeague());

        Map<String, Integer> pointsMap = new HashMap<>();
        Map<String, Integer> placeMap = new HashMap<>();

        for (Match m : ctx.getMatches()) {
            processMatch(m, calculator, pointsMap, placeMap);
        }

        return new MatchProcessingResult(pointsMap, placeMap);
    }

    private void processMatch(Match m,
                              PointsCalculator calculator,
                              Map<String, Integer> pointsMap,
                              Map<String, Integer> placeMap) {

        String p1 = normalize(m.getPlayer1());
        String p2 = normalize(m.getPlayer2());

        int p1Points = calculator.calculatePoints(m);
        pointsMap.merge(p1, p1Points, Integer::sum);

        int p1Place = placementCalculator.calculatePlace(m);
        if (p1Place != 0) placeMap.put(p1, p1Place);

        Match reversed = m.reverse();

        int p2Points = calculator.calculatePoints(reversed);
        pointsMap.merge(p2, p2Points, Integer::sum);

        int p2Place = placementCalculator.calculatePlace(reversed);
        if (p2Place != 0) placeMap.put(p2, p2Place);
    }

    private String normalize(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}