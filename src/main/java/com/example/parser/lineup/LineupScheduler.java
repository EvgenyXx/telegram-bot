package com.example.parser.lineup;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LineupScheduler {

    private final LineupService lineupService;

    @Scheduled(cron = "0 */10 * * * *") // каждые 10 минут
    public void loadTomorrowLineups() {
        lineupService.loadLineups();
    }
}