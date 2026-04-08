package com.example.parser.tournament;

import com.example.parser.domain.dto.ResultDto;
import com.example.parser.domain.model.LeagueType;
import com.example.parser.domain.model.Match;
import com.example.parser.stats.BonusCalculator;
import com.example.parser.stats.PointsCalculator;
import com.example.parser.stats.PointsCalculatorFactory;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupWithdrawStrategy implements TournamentStrategy {

    private final ResultService resultService;
    private final BonusCalculator bonusCalculator;
    private final PointsCalculatorFactory pointsCalculatorFactory;

    @Override
    public boolean isApplicable(Document doc, List<Match> matches) {
        boolean hasRemoved = !doc.select(".removed").isEmpty();
        boolean isGroupStage = !doc.select(".ml_tour_game_group").isEmpty();
        return hasRemoved && isGroupStage;
    }

    @Override
    public ResultService.ParsedResult calculate(Document doc, List<Match> matches) throws Exception {

        // ✅ 1. считаем ВСЕ матчи (как было при 3300)
        ResultService.ParsedResult result =
                resultService.calculateFromMatches(doc, matches);

        // ✅ 2. берём нужный калькулятор
        PointsCalculator calculator =
                pointsCalculatorFactory.getCalculator(
                        LeagueType.valueOf(result.getLeague())
                );

        // 🔥 3. убираем очки за недоигранные матчи
        removeInvalidMatchPoints(result, matches, calculator);

        // ✅ 4. фикс мест
        fixPlacesForGroupWithdraw(result);

        // ✅ 5. пересчёт бонуса
        recalcBonusAndTotal(result);

        return result;
    }

    private void removeInvalidMatchPoints(ResultService.ParsedResult result,
                                          List<Match> matches,
                                          PointsCalculator calculator) {

        for (Match m : matches) {

            boolean isCompleted = m.getScore1() == 4 || m.getScore2() == 4;
            if (isCompleted) continue;

            String p1 = m.getPlayer1();
            String p2 = m.getPlayer2();

            int points1 = calculator.calculatePoints(m);
            int points2 = calculator.calculatePoints(m.reverse());

            for (ResultDto dto : result.getResults()) {

                if (dto.getPlayer().equalsIgnoreCase(p1)) {
                    dto.setTotal(dto.getTotal() - points1);
                }

                if (dto.getPlayer().equalsIgnoreCase(p2)) {
                    dto.setTotal(dto.getTotal() - points2);
                }
            }
        }
    }

    private void fixPlacesForGroupWithdraw(ResultService.ParsedResult result) {
        List<ResultDto> results = result.getResults();

        for (int i = 0; i < results.size(); i++) {
            ResultDto dto = results.get(i);

            if (i == results.size() - 1) dto.setPlace(4);
            else if (i == results.size() - 2) dto.setPlace(3);
            else if (i == 1) dto.setPlace(2);
            else if (i == 0) dto.setPlace(1);
        }
    }

    private void recalcBonusAndTotal(ResultService.ParsedResult result) {
        double nightBonus = result.getNightBonus();

        for (ResultDto dto : result.getResults()) {
            int bonus = bonusCalculator.getBonus(dto.getPlace());

            int purePoints = dto.getTotal() - dto.getBonus();
            int newTotal = purePoints + bonus + (int) nightBonus;

            dto.setBonus(bonus);
            dto.setTotal(newTotal);
        }
    }
}