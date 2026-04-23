package com.example.parser.modules.match.dto;

import com.example.parser.core.model.Match;
import com.example.parser.modules.tournament.domain.TournamentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LiveMatchData {
    private Match match;
    private TournamentStatus status;
    private Match lastMatch;
}