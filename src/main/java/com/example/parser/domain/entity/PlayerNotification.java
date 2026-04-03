package com.example.parser.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "player_notification",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"telegram_id", "tournament_id"}
        )
)
public class PlayerNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_id", nullable = false)
    private Long telegramId;

    @Column(name = "tournament_id", nullable = false)
    private Long tournamentId;

    @Column(nullable = false)
    private String link;

    private LocalDate date;

    /**
     * Время турнира (HH:mm)
     */
    private String time;

    /**
     * Уже отправляли уведомление о новом турнире
     */
    @Column(name = "processed")
    private boolean processed;

    /**
     * Уже отправляли напоминание (за 3 часа)
     */
    @Column(name = "reminder_sent")
    private boolean reminderSent;

    /**
     * Турнир уже был запущен в обработку
     */
    @Column(name = "started")
    private boolean started;

    private boolean finished;
}