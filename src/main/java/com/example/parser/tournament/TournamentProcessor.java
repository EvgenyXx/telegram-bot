package com.example.parser.tournament;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.notification.NotificationService;
import com.example.parser.notification.PlayerNotificationRepository;
import com.example.parser.notification.formatter.TournamentMessageFormatter;
import com.example.parser.player.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class TournamentProcessor {

    private final ResultService resultService;
    private final TournamentResultService tournamentResultService;
    private final PlayerNotificationRepository notificationRepo;
    private final TournamentMessageFormatter formatter;
    private final NotificationService notificationService;

    public void process(PlayerNotification pn) {

        try {
            log.warn("🚀 [PROCESSOR] Старт обработки: {}", pn.getLink());

            ResultService.ParsedResult parsed =
                    resultService.calculateAll(pn.getLink());

            log.warn("📊 [PROCESSOR] tournamentId={}", parsed.getTournamentId());
            log.warn("👥 [PROCESSOR] players={}", parsed.getResults().size());

            // 🔥 ГЛАВНОЕ ИЗМЕНЕНИЕ
            Player player = pn.getPlayer();

            if (player == null) {
                log.warn("❌ [PROCESSOR] Player is null");
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

                String message = formatter.format(parsed.getResults());

                Long telegramId = player.getTelegramId(); // 🔥 ТОЛЬКО ТАК

                notificationService.send(telegramId, message);

                pn.setFinished(true); // 🔥 ВАЖНО
                notificationRepo.save(pn);
            }

        } catch (Exception e) {
            log.error("❌ [PROCESSOR] Error: {}", pn.getLink(), e);
        }
    }
}