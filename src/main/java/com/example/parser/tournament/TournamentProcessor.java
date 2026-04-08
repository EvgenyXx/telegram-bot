package com.example.parser.tournament;

import com.example.parser.domain.entity.PlayerNotification;

import com.example.parser.notification.NotificationService;
import com.example.parser.notification.PlayerNotificationRepository;
import com.example.parser.notification.formatter.TournamentMessageFormatter;

import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;



@RequiredArgsConstructor
@Log4j2
public class TournamentProcessor {

    private final ResultService resultService;
    private final TournamentResultService tournamentResultService;
    private final PlayerService playerService;
    private final PlayerNotificationRepository notificationRepo; // 👈 ДОБАВИЛИ
    private final TournamentMessageFormatter formatter;
    private final NotificationService notificationService; // страховка

    public void process(PlayerNotification pn) {
        try {
            log.warn("🚀 [PROCESSOR] Старт обработки: {}", pn.getLink());

            ResultService.ParsedResult parsed =
                    resultService.calculateAll(pn.getLink());

            log.warn("📊 [PROCESSOR] tournamentId={}", parsed.getTournamentId());
            log.warn("👥 [PROCESSOR] players={}", parsed.getResults().size());

            Player player = playerService.getByTelegramId(pn.getTelegramId());

            if (player == null) {
                log.warn("❌ [PROCESSOR] Player not found: {}", pn.getTelegramId());
                return;
            }

            boolean found = tournamentResultService.processResults(
                    parsed.getResults(),
                    player,
                    parsed.getTournamentId(),
                    parsed.getNightBonus(),
                    parsed.isFinished()
            );

            log.warn("💾 [PROCESSOR] Сохранение результатов, найдено={}", found);

            if (parsed.isFinished()) {
                log.warn("🏁 [PROCESSOR] Турнир завершен");

                boolean isRemoved = parsed.isHasRemovedPlayers();
                String message = formatter.format(
                        parsed.getResults(),isRemoved
                );

                notificationService.send(pn.getTelegramId(), message);

                notificationRepo.save(pn); // этот кусок тоже
            }

        } catch (Exception e) {
            log.error("❌ [PROCESSOR] Error: {}", pn.getLink(), e);
        }
    }
}