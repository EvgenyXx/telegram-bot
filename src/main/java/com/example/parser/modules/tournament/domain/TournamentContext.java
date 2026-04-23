package com.example.parser.modules.tournament.domain;

import com.example.parser.core.model.LeagueType;
import com.example.parser.core.model.Match;
import com.example.parser.modules.tournament.calculation.strategy.removed.RemovedStage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TournamentContext {

    private Long tournamentId;
    private TournamentStatus tournamentStatus;
    private String date;
    private List<Match> matches;
    private LeagueType league;
    private double nightBonus;


    private RemovedStage removedStage;
    private String removedPlayer;


}