package com.example.parser.notification;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.domain.entity.Tournament;
import com.example.parser.player.Player;
import com.example.parser.tournament.ResultService;
import com.example.parser.tournament.TournamentResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentProcessService {

    private final TournamentResultService tournamentResultService;
    private final NotificationService notificationService;

    @Transactional
    public void processTournament(
            List<PlayerNotification> notifications,
            ResultService.ParsedResult parsed
    ) {

        if (notifications == null || notifications.isEmpty()) return;

        Tournament tournament = notifications.get(0).getTournament();
        if (tournament == null) return;

        for (PlayerNotification pn : notifications) {

            Player player = pn.getPlayer(); // ✅ теперь не падает

            if (player == null) continue;

            boolean found = tournamentResultService.processResults(
                    parsed.getResults(),
                    player,
                    parsed.getTournamentId(),
                    parsed.getNightBonus(),
                    true
            );

            if (!found) continue;

            notificationService.send(
                    player.getTelegramId(),
                    "🏁 Турнир завершён, результат обновлён"
            );
        }

        // ✅ помечаем завершённым
        tournament.setFinished(true);
    }
}