package com.example.parser.notification.discovery;

import com.example.parser.lineup.TournamentRepository;
import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.domain.entity.Tournament;
import com.example.parser.notification.NotificationFactory;
import com.example.parser.notification.PlayerNotificationRepository;
import com.example.parser.player.Player;
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