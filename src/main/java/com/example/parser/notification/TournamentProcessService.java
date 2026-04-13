package com.example.parser.notification;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.domain.entity.Tournament;
import com.example.parser.player.Player;
import com.example.parser.tournament.ResultService;
import com.example.parser.tournament.TournamentResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentProcessService {

    private final TournamentResultService tournamentResultService;
    private final NotificationService notificationService;

    @Transactional
    public void processTournament(
            List<PlayerNotification> notifications,
            ResultService.ParsedResult parsed
    ) {

        if (notifications == null || notifications.isEmpty()) {
            log.warn("⏭ processTournament skip: empty notifications");
            return;
        }

        Tournament tournament = notifications.get(0).getTournament();
        if (tournament == null) {
            log.warn("⏭ processTournament skip: tournament is null");
            return;
        }

        log.info("🏁 process finish: tournamentId={}, users={}",
                parsed.getTournamentId(),
                notifications.size());

        int processed = 0;
        int foundCount = 0;
        int notified = 0;
        int failed = 0;

        for (PlayerNotification pn : notifications) {

            Player player = pn.getPlayer();
            if (player == null) continue;

            processed++;

            boolean found = tournamentResultService.processResults(
                    parsed.getResults(),
                    player,
                    parsed.getTournamentId(),
                    parsed.getNightBonus(),
                    true
            );

            if (!found) continue;

            foundCount++;

            try {
                notificationService.send(
                        player.getTelegramId(),
                        "🏁 Турнир завершён, результаты посчитаны"
                );
                notified++;
            } catch (Exception e) {
                failed++;
                log.error("❌ finish send failed: telegramId={}",
                        player.getTelegramId(), e);
            }
        }

        tournament.setFinished(true);

        log.info("✅ process finish done: tournamentId={}, processed={}, found={}, notified={}, failed={}",
                tournament.getExternalId(),
                processed,
                foundCount,
                notified,
                failed);
    }
}