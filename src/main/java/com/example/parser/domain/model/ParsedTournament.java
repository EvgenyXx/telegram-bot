package com.example.parser.domain.model;

import java.util.List;

public class ParsedTournament {

    private Long tournamentId;
    private List<Match> matches;
    private boolean finished;

    public ParsedTournament(Long tournamentId, List<Match> matches, boolean finished) {
        this.tournamentId = tournamentId;
        this.matches = matches;
        this.finished = finished;
    }

    public Long getTournamentId() {
        return tournamentId;
    }

    public List<Match> getMatches() {
        return matches;
    }

    public boolean isFinished() {
        return finished;
    }
}