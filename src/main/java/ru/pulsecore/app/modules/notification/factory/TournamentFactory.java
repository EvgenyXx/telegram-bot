package ru.pulsecore.app.modules.notification.factory;


import ru.pulsecore.app.core.dto.TournamentDto;
import ru.pulsecore.app.modules.tournament.persistence.entity.TournamentEntity;
import org.springframework.stereotype.Component;


import java.time.LocalDate;

@Component
public class TournamentFactory {

    public TournamentEntity create(TournamentDto t) {

        TournamentEntity tournament = new TournamentEntity();

        tournament.setExternalId(t.getId());
        tournament.setLink(t.getLink());
        tournament.setStarted(false);
        tournament.setFinished(false);

        fillDateTime(tournament, t);

        return tournament;
    }

    private void fillDateTime(TournamentEntity tournament, TournamentDto t) {

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