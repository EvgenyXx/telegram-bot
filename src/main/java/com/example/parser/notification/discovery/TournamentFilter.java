package com.example.parser.notification.discovery;

import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.notification.PlayerNotificationRepository;
import com.example.parser.player.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentFilter {

    private final PlayerNotificationRepository notificationRepo;

    public List<TournamentDto> findNew(Player user, List<TournamentDto> tournaments) {

        return tournaments.stream()
                .filter(t -> !notificationRepo
                        .existsByPlayerAndTournament_ExternalId(user, t.getId()))
                .toList();
    }
}