package com.example.parser.lineup;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LineupScheduler {

    private final LineupService lineupService;

    @Scheduled(cron = "0 10 */1 * * *") // в 10 минут каждого часа
    public void loadTomorrowLineups() {
        lineupService.loadLineups();
    }
}