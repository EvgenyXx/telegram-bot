package com.example.parser;


import com.example.parser.lineup.LineupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LineupScheduler {

    private final LineupService lineupService;

    @Scheduled(initialDelay = 5000, fixedRate = 7200000)
    public void loadTomorrowLineups() {
        lineupService.loadLineupsForTomorrow();
    }
}