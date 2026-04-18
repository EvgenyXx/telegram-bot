package com.example.parser.modules.notification.service;

import com.example.parser.modules.notification.repository.PlayerNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationCleanupService {


    private final PlayerNotificationRepository notificationRepository;
    private static final int DAYS_TO_KEEP = 3;

    public void cleanup() {

        LocalDate thresholdDate = LocalDate.now().minusDays(DAYS_TO_KEEP);

        notificationRepository.deleteByTournament_FinishedTrueAndTournament_DateBefore(thresholdDate);
    }
}
