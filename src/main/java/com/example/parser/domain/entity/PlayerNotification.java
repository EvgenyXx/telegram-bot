package com.example.parser.domain.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

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

    private String link;
    private LocalDate date;
    private boolean processed; // уже обработали или нет
}