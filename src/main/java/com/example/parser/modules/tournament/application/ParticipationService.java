package com.example.parser.modules.tournament.application;

import com.example.parser.modules.tournament.domain.ParsedResult;
import org.springframework.stereotype.Service;

@Service
public class ParticipationService {

    public boolean isUserInParsed(ParsedResult parsed, String playerName) {
        if (parsed == null || parsed.getResults() == null) return false;

        return parsed.getResults().stream()
                .anyMatch(r ->
                        r.getPlayer() != null &&
                                r.getPlayer().equalsIgnoreCase(playerName)
                );
    }
}