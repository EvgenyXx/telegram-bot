package com.example.parser.modules.tournament.processor;

import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.tournament.domain.Tournament;
import com.example.parser.modules.notification.service.NotificationService;
import com.example.parser.modules.notification.repository.PlayerNotificationRepository;
import com.example.parser.modules.notification.formatter.TournamentMessageFormatter;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.tournament.service.ResultService;
import com.example.parser.modules.tournament.service.TournamentResultService;
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
            Tournament tournament = pn.getTournament();

            if (tournament == null) {
                log.warn("❌ [PROCESSOR] Tournament is null");
                return;
            }

            String link = tournament.getLink();

            log.warn("🚀 [PROCESSOR] Старт обработки: {}", link);

            ResultService.ParsedResult parsed =
                    resultService.calculateAll(link);

            log.warn("📊 [PROCESSOR] tournamentId={}", parsed.getTournamentId());
            log.warn("👥 [PROCESSOR] players={}", parsed.getResults().size());

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

                Long telegramId = player.getTelegramId();

                notificationService.send(telegramId, message);

                // 🔥 теперь finished хранится в Tournament
                tournament.setFinished(true);

                notificationRepo.save(pn);
            }

        } catch (Exception e) {
            log.error("❌ [PROCESSOR] Error", e);
        }
    }
}