package ru.pulsecore.app.modules.notification.domain;

import ru.pulsecore.app.modules.tournament.persistence.entity.TournamentEntity;
import ru.pulsecore.app.modules.player.domain.Player;
import jakarta.persistence.*;
import lombok.*;

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

    // 🔥 ВАЖНО: теперь это FK на Tournament
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private TournamentEntity tournament;

    @Builder.Default
    @Column(name = "reminder_sent", nullable = false)
    private boolean reminderSent = false;

    @Builder.Default
    @Column(name = "evening_sent", nullable = false)
    private boolean eveningSent = false;

    private Integer hall;
}