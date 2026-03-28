package com.example.parser.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tournament_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 👉 связь с игроком
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    // 👉 имя как в турнире (на всякий случай)
    @Column(nullable = false)
    private String playerName;

    // 👉 сумма
    @Column(nullable = false)
    private Integer amount;

    // 👉 дата турнира
    @Column(nullable = false)
    private LocalDate date;
}