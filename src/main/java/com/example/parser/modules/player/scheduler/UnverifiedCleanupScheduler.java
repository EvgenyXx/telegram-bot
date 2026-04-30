package com.example.parser.modules.player.scheduler;

import com.example.parser.modules.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UnverifiedCleanupScheduler {

    private final PlayerRepository playerRepository;

    @Scheduled(cron = "0 0 3 * * *") // Каждый день в 3:00 ночи
    @Transactional
    public void cleanUnverified() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        int deleted = playerRepository.deleteUnverifiedOlderThan(cutoff);
        log.info("🧹 Удалено неподтверждённых пользователей: {}", deleted);
    }
}