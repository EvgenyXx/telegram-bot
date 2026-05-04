package ru.pulsecore.app.modules.tournament.domain;



import ru.pulsecore.app.core.dto.ResultDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ParsedResult {

    private Long tournamentId;
    private List<ResultDto> results;
    private TournamentStatus status; // ✅ ВОТ ЭТО
    private double nightBonus;

    public boolean isFinished() {
        return status != null && status.isFinished();
    }
}