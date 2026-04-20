package com.example.parser.core.dto;

import com.example.parser.modules.tournament.domain.TournamentLinkStatus;
import com.example.parser.modules.tournament.service.result.ParsedResult;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TournamentLinkResult {

    private TournamentLinkStatus status;
    private ParsedResult parsed;

}