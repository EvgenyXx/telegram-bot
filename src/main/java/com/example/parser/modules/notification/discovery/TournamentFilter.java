package com.example.parser.modules.notification.discovery;

import com.example.parser.core.dto.TournamentDto;
import com.example.parser.modules.notification.repository.PlayerNotificationRepository;
import com.example.parser.modules.player.domain.Player;
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