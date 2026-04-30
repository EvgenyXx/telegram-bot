package com.example.parser.modules.player.domain;

import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.tournament.persistence.entity.TournamentResultEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "players")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, length = 10)
    private String accessCode;

    @Column(length = 6)
    private String verificationCode;

    @Builder.Default
    private boolean verified = false;

    @OneToOne(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private Subscription subscription;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TournamentResultEntity> results = new ArrayList<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerNotification> notifications = new ArrayList<>();

    private LocalDateTime createdAt;

    @Column(name = "is_blocked")
    @Builder.Default
    private boolean isBlocked = false;

    public boolean hasActiveSubscription() {
        return subscription != null && subscription.isActiveNow();
    }
}