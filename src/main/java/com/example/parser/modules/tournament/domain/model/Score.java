package com.example.parser.modules.tournament.domain.model;



import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Score {
    private final int player1;
    private final int player2;
}