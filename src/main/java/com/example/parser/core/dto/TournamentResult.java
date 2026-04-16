package com.example.parser.core.dto;

import com.example.parser.core.model.LeagueType;

import java.util.Map;

public record TournamentResult(LeagueType league, Map<String, Integer> results) {

}