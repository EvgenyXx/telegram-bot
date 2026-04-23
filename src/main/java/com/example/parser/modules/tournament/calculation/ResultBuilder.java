package com.example.parser.modules.tournament.calculation;

import com.example.parser.core.dto.ResultDto;
import com.example.parser.core.stats.BonusCalculator;
import com.example.parser.modules.tournament.domain.MatchProcessingResult;
import com.example.parser.modules.tournament.domain.TournamentContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ResultBuilder {

    private final BonusCalculator bonusCalculator;

    public List<ResultDto> build(MatchProcessingResult matchResult,
                                 TournamentContext ctx) {

        List<ResultDto> results = new ArrayList<>();

        for (String player : matchResult.getPointsMap().keySet()) {

            int place = matchResult.getPlaceMap().getOrDefault(player, 0);
            int bonus = bonusCalculator.getBonus(place);

            int base = matchResult.getPointsMap().get(player) + bonus;
            int total = base + (int) ctx.getNightBonus();

            results.add(new ResultDto(
                    player,
                    place,
                    bonus,
                    total,
                    ctx.getDate()
            ));
        }

        return results;
    }
}