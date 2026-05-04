package ru.pulsecore.app.modules.tournament.api.dto;

import ru.pulsecore.app.core.model.Match;
import ru.pulsecore.app.modules.tournament.domain.TournamentStatus;

import java.util.List;

public record ParsedTournament(Long tournamentId, List<Match> matches, TournamentStatus status) {


}