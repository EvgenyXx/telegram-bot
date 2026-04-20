package com.example.parser.modules.tournament.service.result;

import com.example.parser.core.model.LeagueType;
import com.example.parser.core.model.Match;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TournamentContext {

    private Long tournamentId;
    private TournamentStatus tournamentStatus;
    private String date;
    private List<Match> matches;
    private LeagueType league;
    private double nightBonus;
    private boolean hasRemovedPlayers;
}