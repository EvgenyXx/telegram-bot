package com.example.parser.modules.player.domain;


import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.tournament.persistence.entity.TournamentResultEntity;
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

    @Column(nullable = false, unique = true)
    private Long telegramId;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TournamentResultEntity> results = new ArrayList<>();

    // 🔥 (опционально, но удобно)
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerNotification> notifications = new ArrayList<>();

    private LocalDateTime createdAt;

    @Column(name = "is_blocked")
    private boolean isBlocked;
}