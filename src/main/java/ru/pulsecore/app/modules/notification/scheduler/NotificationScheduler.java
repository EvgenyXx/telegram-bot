package ru.pulsecore.app.modules.notification.scheduler;

import ru.pulsecore.app.modules.notification.discovery.TournamentDiscoveryService;
import ru.pulsecore.app.modules.player.domain.Player;
import ru.pulsecore.app.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final PlayerService playerService;
    private final TournamentDiscoveryService discoveryService;

    @Scheduled(fixedDelay = 600000)
    public void checkAllUsers() {
        List<Player> players = playerService.getAll();

        if (players.isEmpty()) {
            log.debug("Scheduler: no players to check");
            return;
        }

        int errors = 0;

        for (Player player : players) {
            try {
                discoveryService.checkNewTournaments(player.getId());
            } catch (Exception e) {
                errors++;
                log.error("Failed to check tournaments for player {}", player.getId(), e);
            }
        }

        if (errors > 0) {
            log.warn("Scheduler completed with {} errors out of {} players", errors, players.size());
        }
    }
}