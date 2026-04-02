package com.example.parser.player;

import com.example.parser.domain.entity.PlayerSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerSubscriptionRepository extends JpaRepository<PlayerSubscription, Long> {
    List<PlayerSubscription> findAll();
}