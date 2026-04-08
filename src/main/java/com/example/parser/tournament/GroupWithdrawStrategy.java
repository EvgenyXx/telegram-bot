package com.example.parser.tournament;

import com.example.parser.domain.dto.ResultDto;
import com.example.parser.domain.model.Match;
import com.example.parser.stats.BonusCalculator;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupWithdrawStrategy implements TournamentStrategy {

    private final ResultService resultService;
    private final BonusCalculator bonusCalculator;

    @Override
    public boolean isApplicable(Document doc, List<Match> matches) {
        boolean hasRemoved = !doc.select(".removed").isEmpty();
        boolean isGroupStage = !doc.select(".ml_tour_game_group").isEmpty();
        return hasRemoved && isGroupStage;
    }

    @Override
    public ResultService.ParsedResult calculate(Document doc, List<Match> matches) throws Exception {

        // ✅ 1. только завершённые матчи
        List<Match> filtered = matches.stream()
                .filter(this::isCompletedMatch)
                .toList();

        // ✅ 2. базовый расчёт
        ResultService.ParsedResult result =
                resultService.calculateFromMatches(doc, filtered);

        // ✅ 3. фикс мест
        fixPlacesForGroupWithdraw(result);

        // ✅ 4. пересчёт бонуса
        recalcBonusAndTotal(result);

        // 🔥 5. КРИТИЧНО: пересортировка после изменений
        result.getResults().sort((a, b) -> Integer.compare(b.getTotal(), a.getTotal()));

        return result;
    }

    private boolean isCompletedMatch(Match m) {
        return m.getScore1() == 4 || m.getScore2() == 4;
    }

    private void fixPlacesForGroupWithdraw(ResultService.ParsedResult result) {
        List<ResultDto> results = result.getResults();

        for (int i = 0; i < results.size(); i++) {
            ResultDto dto = results.get(i);

            if (i == results.size() - 1) {
                dto.setPlace(4);
            } else if (i == results.size() - 2) {
                dto.setPlace(3);
            } else if (i == 1) {
                dto.setPlace(2);
            } else if (i == 0) {
                dto.setPlace(1);
            }
        }
    }

    private void recalcBonusAndTotal(ResultService.ParsedResult result) {
        double nightBonus = result.getNightBonus();

        for (ResultDto dto : result.getResults()) {

            int place = dto.getPlace();
            int bonus = bonusCalculator.getBonus(place);

            // чистые очки без бонуса
            int purePoints = dto.getTotal() - dto.getBonus();

            // новый total
            int newTotal = purePoints + bonus + (int) nightBonus;

            dto.setBonus(bonus);
            dto.setTotal(newTotal);
        }
    }
}