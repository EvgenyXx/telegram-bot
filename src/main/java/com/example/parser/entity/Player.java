package com.example.parser.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 👉 telegram chat id (уникальный пользователь)
    @Column(nullable = false, unique = true)
    private Long telegramId;

    // 👉 имя которое он ввел
    @Column(nullable = false)
    private String name;

    // 👉 связь с результатами
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TournamentResultEntity> results = new ArrayList<>();


    private LocalDateTime createdAt;
}