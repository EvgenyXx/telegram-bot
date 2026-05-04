package ru.pulsecore.app.modules.lineup.validator;

import ru.pulsecore.app.core.dto.TournamentDto;
import org.springframework.stereotype.Component;

@Component
public class TournamentValidator {

    public boolean isValid(TournamentDto t) {
        return t.getPlayers() != null && !t.getPlayers().isEmpty();
    }
}