package com.example.parser.modules.player.api.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class TournamentListResponse {
    private String playerName;
    private String start;
    private String end;
    private int count;
    private double sum;
    private List<TournamentResultItem> tournaments;

    @Data
    @Builder
    public static class TournamentResultItem {
        private String date;
        private Double amount;
    }
}