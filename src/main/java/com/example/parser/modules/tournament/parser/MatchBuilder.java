package com.example.parser.modules.tournament.parser;

import com.example.parser.core.model.Match;
import com.example.parser.modules.tournament.model.Score;
import org.springframework.stereotype.Component;

@Component
public class MatchBuilder {

    public Match build(String stage,
                       String player1,
                       String player2,
                       Score score,
                       String sets,
                       String league,
                       String table) {

        if (score == null) {
            return null;
        }

        Match match = new Match();
        match.setStage(stage);
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setScore1(score.getPlayer1());
        match.setScore2(score.getPlayer2());
        match.setSetsDetails(sets);
        match.setLeague(league);
        match.setTable(table);

        return match;
    }
}