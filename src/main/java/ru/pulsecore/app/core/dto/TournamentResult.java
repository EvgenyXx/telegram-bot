package ru.pulsecore.app.core.dto;

import ru.pulsecore.app.core.model.LeagueType;

import java.util.Map;

public record TournamentResult(LeagueType league, Map<String, Integer> results) {

}