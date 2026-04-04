package com.example.parser.notification;

import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.domain.entity.PlayerNotification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class NotificationFactory {

    public PlayerNotification create(Long telegramId, TournamentDto t) {
        PlayerNotification pn = new PlayerNotification();

        pn.setTelegramId(telegramId);
        pn.setTournamentId(t.getId());
        pn.setLink(t.getLink());

        fillDateTime(pn, t);



        return pn;
    }

    private void fillDateTime(PlayerNotification pn, TournamentDto t) {
        if (t.getDate() == null || t.getDate().getDate() == null) {
            return;
        }

        String raw = t.getDate().getDate();

        if (raw.length() >= 10) {
            pn.setDate(LocalDate.parse(raw.substring(0, 10)));
        }

        if (raw.length() >= 16) {
            pn.setTime(raw.substring(11, 16));
        }
    }



}