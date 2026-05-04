package ru.pulsecore.app.modules.lineup.scheduler;


import ru.pulsecore.app.modules.lineup.service.LineupService;
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

    @Scheduled(cron = "0 0 3 * * *") // каждый день в 3 часа ночи
    public void cleanup() {
        lineupService.cleanupOld();
    }
}