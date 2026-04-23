package com.example.parser.modules.notification.discovery;

import com.example.parser.core.dto.TournamentDto;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.tournament.application.UpcomingTournamentService;
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