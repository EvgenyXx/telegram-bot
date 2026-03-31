package com.example.parser.domain.dto;

import com.example.parser.domain.model.LeagueType;

import java.util.Map;

public class TournamentResult {
    private final LeagueType league;
    private final Map<String, Integer> results;

    public TournamentResult(LeagueType league, Map<String, Integer> results) {
        this.league = league;
        this.results = results;
    }

    public LeagueType getLeague() {
        return league;
    }

    public Map<String, Integer> getResults() {
        return results;
    }
}