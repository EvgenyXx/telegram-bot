package com.example.parser.modules.tournament.dto;

import com.example.parser.core.dto.ResultDto;
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