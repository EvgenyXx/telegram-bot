package com.example.parser.modules.lineup.scheduler;


import com.example.parser.modules.lineup.service.LineupService;
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