package com.example.parser.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"telegramId", "tournamentId"}
        )
)
public class PlayerNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long telegramId;

    private Long tournamentId;
}