package com.example.parser.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "tournament_results",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"player_id", "tournament_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tournament_id")
    private Long tournamentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private String playerName;

    // 🔥 было Integer
    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate date;

    private boolean isNight;
    private Double bonus;
}