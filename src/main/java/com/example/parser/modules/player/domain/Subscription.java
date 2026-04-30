package com.example.parser.modules.player.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "subscription")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "player_id", unique = true)
    private Player player;

    @Builder.Default
    private boolean active = false;

    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isActiveNow() {
        return active && expiresAt != null && expiresAt.isAfter(LocalDateTime.now());
    }

    public void activate(int days) {
        this.active = true;
        this.startedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusDays(days);
    }
}