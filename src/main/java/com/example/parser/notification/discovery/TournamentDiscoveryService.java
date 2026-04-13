package com.example.parser.notification.discovery;

import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.notification.NotificationService;
import com.example.parser.notification.formatter.TournamentDiscoveryMessageFormatter;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentDiscoveryService {

    private final PlayerService userService;
    private final TournamentFinder finder;
    private final TournamentFilter filter;
    private final TournamentSaver saver;
    private final NotificationService notificationService;
    private final TournamentDiscoveryMessageFormatter messageFormatter;

    public void checkNewTournaments(Long telegramId) {

        Player user = userService.getByTelegramId(telegramId);

        if (user == null) {
            log.warn("User not found for telegramId={}", telegramId);
            return;
        }

        List<TournamentDto> tournaments = finder.find(user);
        if (tournaments.isEmpty()) return;

        List<TournamentDto> newTournaments = filter.findNew(user, tournaments);
        if (newTournaments.isEmpty()) return;

        saver.save(user, newTournaments);

        notificationService.send(telegramId, messageFormatter.format(newTournaments));
    }


}