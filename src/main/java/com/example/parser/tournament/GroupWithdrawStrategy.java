package com.example.parser.tournament;

import com.example.parser.domain.dto.ResultDto;
import com.example.parser.domain.model.Match;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupWithdrawStrategy implements TournamentStrategy {

    private final ResultService resultService;

    @Override
    public boolean isApplicable(Document doc, List<Match> matches) {
        boolean hasRemoved = !doc.select(".removed").isEmpty();
        boolean isGroupStage = !doc.select(".ml_tour_game_group").isEmpty();
        return hasRemoved && isGroupStage;
    }

    @Override
    public ResultService.ParsedResult calculate(Document doc, List<Match> matches) throws Exception {

        log.info("=== GroupWithdrawStrategy START ===");

        // 👉 1. фильтруем только завершённые матчи
        List<Match> filtered = matches.stream()
                .filter(this::isCompletedMatch)
                .toList();

        log.info("Total matches: {}", matches.size());
        log.info("Completed matches: {}", filtered.size());

        filtered.forEach(m ->
                log.info("MATCH: {} vs {} | {}:{}",
                        m.getPlayer1(),
                        m.getPlayer2(),
                        m.getScore1(),
                        m.getScore2())
        );

        // 👉 2. базовый расчёт
        ResultService.ParsedResult result =
                resultService.calculateFromMatches(doc, filtered);

        log.info("=== After base calculation ===");
        result.getResults().forEach(r ->
                log.info("Player: {} | total: {} | bonus: {}",
                        r.getPlayer(),
                        r.getTotal(),
                        r.getBonus())
        );

        // 👉 3. ДОБАВЛЯЕМ снявшегося если он пропал
        addWithdrawnPlayerIfMissing(result, doc);

        // 👉 4. фикс мест
        fixPlacesForGroupWithdraw(result, doc);

        log.info("=== FINAL RESULT ===");
        result.getResults().forEach(r ->
                log.info("Player: {} | place: {} | total: {} | bonus: {}",
                        r.getPlayer(),
                        r.getPlace(),
                        r.getTotal(),
                        r.getBonus())
        );

        log.info("=== GroupWithdrawStrategy END ===");

        return result;
    }

    // ------------------------------------------------------

    private String getWithdrawnPlayer(Document doc) {
        return doc.select(".removed")
                .text()
                .toLowerCase()
                .trim();
    }

    private boolean isCompletedMatch(Match m) {
        return m.getScore1() == 4 || m.getScore2() == 4;
    }

    /**
     * 👉 если игрок снялся и не попал в results — возвращаем его вручную
     */
    private void addWithdrawnPlayerIfMissing(ResultService.ParsedResult result, Document doc) {
        String withdrawn = getWithdrawnPlayer(doc);

        boolean exists = result.getResults().stream()
                .anyMatch(r -> r.getPlayer().equalsIgnoreCase(withdrawn));

        if (!exists) {
            log.warn("Withdrawn player NOT found in results, adding manually: {}", withdrawn);

            ResultDto dto = new ResultDto();
            dto.setPlayer(withdrawn);
            dto.setTotal(0);
            dto.setBonus(0);
            dto.setPlace(4);

            result.getResults().add(dto);
        }
    }

    /**
     * 👉 корректное распределение мест
     */
    private void fixPlacesForGroupWithdraw(ResultService.ParsedResult result, Document doc) {
        List<ResultDto> results = result.getResults();
        String withdrawn = getWithdrawnPlayer(doc);

        // 👉 сортируем по очкам перед расстановкой мест
        results.sort((a, b) -> Double.compare(b.getTotal(), a.getTotal()));

        log.info("Sorting results before assigning places...");

        int place = 1;

        for (ResultDto dto : results) {

            if (dto.getPlayer().equalsIgnoreCase(withdrawn)) {
                dto.setPlace(4);
                dto.setTotal(0);
                dto.setBonus(0);

                log.info("Withdrawn player fixed: {}", dto.getPlayer());
                continue;
            }

            dto.setPlace(place++);

            log.info("Assigned place {} to {}", dto.getPlace(), dto.getPlayer());
        }
    }
}