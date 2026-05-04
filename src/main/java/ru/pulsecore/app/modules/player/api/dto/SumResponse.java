package ru.pulsecore.app.modules.player.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SumResponse {
    private String playerName;
    private String start;
    private String end;
    private Double sum;
    private Double average;
    private Double minusThreePercent;
    private Long count;
}