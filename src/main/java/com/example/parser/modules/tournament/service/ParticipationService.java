package com.example.parser.modules.tournament.service;

import org.springframework.stereotype.Service;

@Service
public class ParticipationService {

    public boolean isUserInParsed(ResultService.ParsedResult parsed, String playerName) {
        if (parsed == null || parsed.getResults() == null) return false;

        return parsed.getResults().stream()
                .anyMatch(r ->
                        r.getPlayer() != null &&
                                r.getPlayer().equalsIgnoreCase(playerName)
                );
    }
}