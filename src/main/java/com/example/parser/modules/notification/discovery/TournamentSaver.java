package com.example.parser.modules.notification.discovery;

import com.example.parser.modules.tournament.repository.TournamentRepository;
import com.example.parser.core.dto.TournamentDto;
import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.tournament.domain.Tournament;
import com.example.parser.modules.notification.factory.NotificationFactory;
import com.example.parser.modules.notification.factory.TournamentFactory;
import com.example.parser.modules.notification.repository.PlayerNotificationRepository;
import com.example.parser.modules.player.domain.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentSaver {

    private final TournamentRepository tournamentRepository;
    private final NotificationFactory notificationFactory;
    private final PlayerNotificationRepository notificationRepo;
    private final TournamentFactory tournamentFactory;

    public void save(Player user, List<TournamentDto> tournaments) {

        for (TournamentDto t : tournaments) {

            Tournament tournament = tournamentRepository
                    .findByExternalId(t.getId())
                    .orElseGet(() ->
                            tournamentRepository.save(
                                    tournamentFactory.create(t)
                            )
                    );

            PlayerNotification pn = notificationFactory.create(user, tournament, t);

            notificationRepo.save(pn);
        }
    }
}