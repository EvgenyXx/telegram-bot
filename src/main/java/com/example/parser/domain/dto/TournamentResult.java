package com.example.parser.domain.dto;

import com.example.parser.domain.model.LeagueType;
import lombok.Getter;

import java.util.Map;

@Getter
public class TournamentResult {
    private final LeagueType league;
    private final Map<String, Integer> results;

    public TournamentResult(LeagueType league, Map<String, Integer> results) {
        this.league = league;
        this.results = results;
    }

}