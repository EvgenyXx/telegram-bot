package com.example.parser.tournament;

import com.example.parser.domain.dto.ResultDto;
import com.example.parser.domain.model.Match;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupWithdrawStrategy implements TournamentStrategy {

    private final ResultService resultService;

    @Override
    public boolean isApplicable(Document doc, List<Match> matches) {
        // есть снятие
        boolean hasRemoved = !doc.select(".removed").isEmpty();

        // есть групповой этап
        boolean isGroupStage = !doc.select(".ml_tour_game_group").isEmpty();

        return hasRemoved && isGroupStage;
    }

    @Override
    public ResultService.ParsedResult calculate(Document doc, List<Match> matches) throws Exception {
        // 👉 1. фильтруем матчи (убираем технические)
        List<Match> filtered = matches.stream()
                .filter(this::isCompletedMatch)
                .toList();

        // 👉 2. считаем как обычный турнир
        ResultService.ParsedResult result =
                resultService.calculateFromMatches(doc, filtered);

        // 👉 3. правим места вручную (твоя логика)
        fixPlacesForGroupWithdraw(result);

        return result;
    }

    private boolean isCompletedMatch(Match m) {
        return m.getScore1() == 4 || m.getScore2() == 4;
    }

    private void fixPlacesForGroupWithdraw(ResultService.ParsedResult result) {

        List<ResultDto> results = result.getResults();

        // сортировка уже есть, просто проставим места
        for (int i = 0; i < results.size(); i++) {
            ResultDto dto = results.get(i);

            if (i == results.size() - 1) {
                dto.setPlace(4); // снявшийся
                dto.setTotal(0);
                dto.setBonus(0);
            } else if (i == results.size() - 2) {
                dto.setPlace(3);
            } else if (i == 1) {
                dto.setPlace(2);
            } else if (i == 0) {
                dto.setPlace(1);
            }
        }
    }
}