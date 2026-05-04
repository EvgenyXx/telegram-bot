package ru.pulsecore.app.modules.tournament.mapper;

import ru.pulsecore.app.modules.tournament.persistence.entity.TournamentEntity;
import ru.pulsecore.app.modules.tournament.domain.TournamentStatus;
import org.springframework.stereotype.Component;

@Component
public class TournamentStatusMapper {

    public void apply(TournamentEntity t, TournamentStatus status) {
        switch (status) {
            case FINISHED -> {
                t.setFinished(true);
                t.setStarted(true);
                t.setCancelled(false);
            }
            case IN_PROGRESS, STARTED -> {
                t.setStarted(true);
                t.setFinished(false);
                t.setCancelled(false);
            }
            case NOT_STARTED -> {
                t.setStarted(false);
                t.setFinished(false);
                t.setCancelled(false);
            }
            case CANCELLED -> {
                t.setCancelled(true);
                t.setFinished(false);
                t.setStarted(false);
            }
        }
    }
}
