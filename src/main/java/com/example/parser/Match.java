package com.example.parser;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Match {
    private String stage;
    private String player1;
    private String player2;
    private int score1;
    private int score2;

    // getters/setters
    public Match reverse() {
        Match m = new Match();
        m.setPlayer1(this.player2);
        m.setPlayer2(this.player1);
        m.setScore1(this.score2);
        m.setScore2(this.score1);
        m.setStage(this.stage);
        return m;
    }

}