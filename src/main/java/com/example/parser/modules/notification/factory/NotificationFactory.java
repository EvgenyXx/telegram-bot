package com.example.parser.modules.notification.factory;

import com.example.parser.core.dto.TournamentDto;
import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.tournament.domain.Tournament;
import com.example.parser.modules.player.domain.Player;
import org.springframework.stereotype.Component;

@Component
public class NotificationFactory {

    public PlayerNotification create(Player player, Tournament tournament, TournamentDto t) {
        PlayerNotification pn = new PlayerNotification();

        pn.setPlayer(player);
        pn.setTournament(tournament);
        pn.setHall(t.getHallNumber());

        return pn;
    }
}