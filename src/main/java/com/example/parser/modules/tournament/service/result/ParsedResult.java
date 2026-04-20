package com.example.parser.modules.tournament.service.result;



import com.example.parser.core.dto.ResultDto;
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
    private boolean hasRemovedPlayers;
}