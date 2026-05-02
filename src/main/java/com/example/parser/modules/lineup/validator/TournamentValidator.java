package com.example.parser.modules.lineup.validator;

import com.example.parser.core.dto.TournamentDto;
import org.springframework.stereotype.Component;

@Component
public class TournamentValidator {

    public boolean isValid(TournamentDto t) {
        return t.getPlayers() != null && !t.getPlayers().isEmpty();
    }
}