package com.example.parser.core.dto;

import com.example.parser.modules.tournament.service.ResultService;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TournamentLinkResult {
    private ResultService.ParsedResult parsed;
    private boolean alreadyExists;
    private boolean found;
}