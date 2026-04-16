package com.example.parser.modules.player.repository;

import com.example.parser.modules.player.domain.PlayerSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerSubscriptionRepository extends JpaRepository<PlayerSubscription, Long> {
    List<PlayerSubscription> findAll();
}