package ru.pulsecore.app.modules.player.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpcomingLineupDto {
    private String date;
    private String time;
    private String league;
    private boolean inLineup;
    private String players;
    private boolean isSoon;
}