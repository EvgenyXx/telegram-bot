package com.example.parser.domain.entity;

import com.example.parser.player.Player;
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
                columnNames = {"player_id", "tournament_id"}
        )
)
public class PlayerNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // связь с игроком
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "tournament_id", nullable = false)
    private Long tournamentId;

    @Column(nullable = false)
    private String link;

    private LocalDate date;

    private String time;

    @Builder.Default
    @Column(name = "reminder_sent", nullable = false)
    private boolean reminderSent = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean started = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean finished = false;

    @Builder.Default
    @Column(name = "evening_sent", nullable = false)
    private boolean eveningSent = false;

    private Integer hall;
}