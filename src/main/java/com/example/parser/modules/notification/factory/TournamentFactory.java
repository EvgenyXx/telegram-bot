package com.example.parser.modules.notification.factory;


import com.example.parser.core.dto.TournamentDto;
import com.example.parser.modules.tournament.domain.Tournament;
import org.springframework.stereotype.Component;


import java.time.LocalDate;

@Component
public class TournamentFactory {

    public Tournament create(TournamentDto t) {

        Tournament tournament = new Tournament();

        tournament.setExternalId(t.getId());
        tournament.setLink(t.getLink());
        tournament.setStarted(false);
        tournament.setFinished(false);

        fillDateTime(tournament, t);

        return tournament;
    }

    private void fillDateTime(Tournament tournament, TournamentDto t) {

        if (t.getDate() == null || t.getDate().getDate() == null) return;

        String raw = t.getDate().getDate();

        if (raw.length() >= 10) {
            tournament.setDate(LocalDate.parse(raw.substring(0, 10)));
        }

        if (raw.length() >= 16) {
            tournament.setTime(raw.substring(11, 16));
        }
    }
}