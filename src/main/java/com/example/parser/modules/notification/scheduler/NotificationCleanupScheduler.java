package com.example.parser.modules.notification.scheduler;

import com.example.parser.modules.notification.repository.PlayerNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

// 🧹 Чистит старые завершенные турниры
// Каждый день в 03:00 удаляет:
// - finished = true
// - дата турнира старше 7 дней
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationCleanupScheduler {

    private final PlayerNotificationRepository notificationRepo;

    private static final int DAYS_TO_KEEP = 3;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanup() {
        LocalDate thresholdDate = LocalDate.now().minusDays(DAYS_TO_KEEP);

        notificationRepo.deleteByTournament_FinishedTrueAndTournament_DateBefore(thresholdDate);

        log.warn("🧹 CLEANUP DONE. Deleted old records before: {}", thresholdDate);
    }
}