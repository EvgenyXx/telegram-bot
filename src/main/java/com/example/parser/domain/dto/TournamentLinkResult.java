package com.example.parser.domain.dto;

import com.example.parser.tournament.ResultService;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TournamentLinkResult {
    private ResultService.ParsedResult parsed;
    private boolean alreadyExists;
    private boolean found;
}