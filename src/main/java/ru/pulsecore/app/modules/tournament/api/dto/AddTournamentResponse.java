package ru.pulsecore.app.modules.tournament.api.dto;

import ru.pulsecore.app.core.dto.ResultDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AddTournamentResponse {
    private String message;
    private Long tournamentId;
    private int resultsCount;
    private List<ResultDto> results;
}