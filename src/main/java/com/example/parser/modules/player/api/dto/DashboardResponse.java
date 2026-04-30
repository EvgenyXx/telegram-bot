package com.example.parser.modules.player.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardResponse {
    private String playerName;
    private LastResultDto lastResult;
    private NextTournamentDto nextTournament;
    private SubscriptionInfoDto subscription;
}