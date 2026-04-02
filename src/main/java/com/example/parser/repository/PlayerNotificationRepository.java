package com.example.parser.repository;

import com.example.parser.domain.entity.PlayerNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PlayerNotificationRepository
        extends JpaRepository<PlayerNotification, Long> {

    boolean existsByTelegramIdAndTournamentId(Long telegramId, Long tournamentId);


    List<PlayerNotification> findAllByProcessedFalse();

    List<PlayerNotification> findAllByDate(LocalDate date);
}