package com.example.parser.core.dto;

import com.example.parser.modules.tournament.persistence.entity.TournamentLinkStatus;
import com.example.parser.modules.tournament.domain.ParsedResult;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TournamentLinkResult {

    private TournamentLinkStatus status;
    private ParsedResult parsed;

}