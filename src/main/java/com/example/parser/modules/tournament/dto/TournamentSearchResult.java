package com.example.parser.modules.tournament.dto;

import com.example.parser.core.dto.TournamentDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TournamentSearchResult {
    private TournamentDto tournament;
    private boolean saved;
}