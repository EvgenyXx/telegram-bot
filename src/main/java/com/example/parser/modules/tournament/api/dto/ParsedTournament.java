package com.example.parser.modules.tournament.api.dto;

import com.example.parser.core.model.Match;
import com.example.parser.modules.tournament.domain.TournamentStatus;

import java.util.List;

public record ParsedTournament(Long tournamentId, List<Match> matches, TournamentStatus status) {


}