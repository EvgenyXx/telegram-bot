package com.example.parser.modules.player.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NextTournamentDto {
    private String date;
    private String time;
    private String link;
    private String hall;
}