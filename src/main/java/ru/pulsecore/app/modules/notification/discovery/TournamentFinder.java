package ru.pulsecore.app.modules.notification.discovery;

import ru.pulsecore.app.core.dto.TournamentDto;
import ru.pulsecore.app.modules.player.domain.Player;
import ru.pulsecore.app.modules.tournament.application.UpcomingTournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentFinder {

    private final UpcomingTournamentService tournamentService;

    public List<TournamentDto> find(Player user) {
        return tournamentService.findPlayerTournaments(user.getName());
    }
}