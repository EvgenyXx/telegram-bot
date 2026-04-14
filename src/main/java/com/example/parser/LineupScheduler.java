package com.example.parser;


import com.example.parser.lineup.LineupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LineupScheduler {

    private final LineupService lineupService;

    // каждый день в 23:00
    @Scheduled(cron = "0 0 */2 * * *")
    public void loadTomorrowLineups() {
        lineupService.loadLineupsForTomorrow();
    }
}