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
            log.warn("⏭ SKIP: notifications empty");
            return;
        }

        Tournament tournament = notifications.get(0).getTournament();

        if (tournament == null) {
            log.warn("⏭ SKIP: tournament is null");
            return;
        }

        log.warn("🏁 PROCESS FINISH: tournamentId={}, users={}",
                parsed.getTournamentId(),
                notifications.size());

        for (PlayerNotification pn : notifications) {

            Player player = pn.getPlayer();

            if (player == null) {
                log.warn("⏭ SKIP player: null (notificationId={})", pn.getId());
                continue;
            }

            log.warn("🔍 CHECK player={} (tgId={})",
                    player.getId(),
                    player.getTelegramId());

            boolean found = tournamentResultService.processResults(
                    parsed.getResults(),
                    player,
                    parsed.getTournamentId(),
                    parsed.getNightBonus(),
                    true
            );

            log.warn("📊 RESULT processed: player={}, found={}",
                    player.getId(),
                    found);

            if (!found) {
                log.warn("⏭ SKIP notify: player not in results (playerId={})",
                        player.getId());
                continue;
            }

            try {
                log.warn("📨 SEND FINISH → telegramId={}",
                        player.getTelegramId());

                notificationService.send(
                        player.getTelegramId(),
                        "🏁 Турнир завершён, результаты посчитаны"
                );

                log.warn("✅ SENT FINISH → telegramId={}",
                        player.getTelegramId());

            } catch (Exception e) {
                log.error("❌ FINISH SEND FAILED → telegramId={}",
                        player.getTelegramId(),
                        e);
            }
        }

        // ✅ помечаем завершённым
        tournament.setFinished(true);

        log.warn("✅ TOURNAMENT MARKED FINISHED → id={}",
                tournament.getExternalId());
    }
}