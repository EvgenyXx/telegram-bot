package ru.pulsecore.app.modules.tournament.parser;

import ru.pulsecore.app.core.model.Match;
import ru.pulsecore.app.modules.tournament.domain.model.Score;
import org.springframework.stereotype.Component;

@Component
public class MatchBuilder {

    public Match build(String stage,
                       String player1,
                       String player2,
                       Score score,
                       String sets,
                       String league,
                       String table,
                       String status) {

        Match match = new Match();

        match.setStage(stage);
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setSetsDetails(sets);
        match.setLeague(league);
        match.setTable(table);
        match.setStatus(status);

        // ✅ ВАЖНО: допускаем null score
        if (score != null) {
            match.setScore1(score.getPlayer1());
            match.setScore2(score.getPlayer2());
        } else {
            // 🔥 для отменённых матчей
            match.setScore1(0);
            match.setScore2(0);
        }

        return match;
    }
}