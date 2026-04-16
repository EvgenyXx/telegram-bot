package com.example.parser.modules.lineup.validator;
import com.example.parser.core.dto.TournamentDto;
import org.springframework.stereotype.Component;

@Component
public class TournamentValidator {

    public boolean isValid(TournamentDto t) {
        Integer hallNumber = extractHallNumber(t.getHall());

        return hallNumber != null
                && (hallNumber == 10 || hallNumber == 11)
                && t.getPlayers() != null
                && !t.getPlayers().isEmpty();
    }

    private Integer extractHallNumber(String hall) {
        if (hall == null) return null;

        try {
            String digits = hall.replaceAll("\\D+", "");
            return digits.isEmpty() ? null : Integer.parseInt(digits);
        } catch (Exception e) {
            return null;
        }
    }
}