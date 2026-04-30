package com.example.parser.modules.notification.discovery;

import com.example.parser.core.dto.TournamentDto;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentDiscoveryService {

    private final PlayerService userService;
    private final TournamentFinder finder;
    private final TournamentFilter filter;
    private final TournamentSaver saver;

    public void checkNewTournaments(UUID playerId) {
        Player user = userService.findById(playerId);
        if (user == null) {
            log.warn("User not found for playerId={}", playerId);
            return;
        }

        List<TournamentDto> tournaments = finder.find(user);
        if (tournaments.isEmpty()) return;

        List<TournamentDto> newTournaments = filter.findNew(user, tournaments);
        if (newTournaments.isEmpty()) return;

        saver.save(user, newTournaments);
        log.info("🔍 Discovered {} new tournaments for player={}", newTournaments.size(), user.getName());
    }
}