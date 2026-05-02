package com.example.parser.modules.player.api.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private String playerName;
    private LastResultDto lastResult;
    private List<UpcomingLineupDto> upcomingLineups;
    private SubscriptionInfoDto subscription;
}