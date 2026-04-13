package com.example.parser.notification.discovery;

import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.player.Player;
import com.example.parser.tournament.UpcomingTournamentService;
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