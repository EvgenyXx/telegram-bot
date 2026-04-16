package com.example.parser.modules.lineup.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "lineup",
        indexes = {
                @Index(name = "idx_lineup_date_city", columnList = "date, city")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lineup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Лига (A, B, C)
     */
    @Column(nullable = false, length = 50)
    private String league;

    /**
     * Время начала (например 10:00)
     */
    @Column(nullable = false, length = 10)
    private String time;

    /**
     * Игроки одной строкой:
     * "Иван, Петр, Саша"
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String players;

    /**
     * Дата турнира (для фильтрации и очистки)
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Город (Ростов и т.д.)
     */
    @Column(nullable = false, length = 100)
    private String city;



}