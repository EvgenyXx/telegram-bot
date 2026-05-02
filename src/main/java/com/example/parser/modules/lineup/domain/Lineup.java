package com.example.parser.modules.lineup.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "lineup",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"league", "time", "date"})
        },
        indexes = {
                @Index(name = "idx_lineup_date", columnList = "date")
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

    @Column(nullable = false, length = 50)
    private String league;

    @Column(nullable = false, length = 10)
    private String time;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String players;

    @Column(nullable = false)
    private LocalDate date;
}