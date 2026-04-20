package com.example.parser.modules.tournament.dto;

import com.example.parser.core.model.Match;
import com.example.parser.modules.tournament.service.result.TournamentStatus;

import java.util.List;

public record ParsedTournament(Long tournamentId, List<Match> matches, TournamentStatus status) {


}