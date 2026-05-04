package ru.pulsecore.app.modules.player.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LastResultDto {
    private String date;
    private Double amount;
    private String tournamentLink;
}