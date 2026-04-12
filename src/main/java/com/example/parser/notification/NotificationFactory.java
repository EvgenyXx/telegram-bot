package com.example.parser.notification;

import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.domain.entity.Tournament;
import com.example.parser.player.Player;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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