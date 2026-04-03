package com.example.parser.tournament;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.domain.model.ParsedTournament;
import com.example.parser.notification.PlayerNotificationRepository;
import com.example.parser.parser.ParserService;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class TournamentProcessor {

    private final ResultService resultService;
    private final TournamentResultService tournamentResultService;
    private final PlayerService playerService;
    private final PlayerNotificationRepository notificationRepo; // 👈 ДОБАВИЛИ

    public void process(PlayerNotification pn) {
        try {
            log.info("🚀 Start: {}", pn.getLink());

            ResultService.ParsedResult parsed =
                    resultService.calculateAll(pn.getLink());
            log.warn("📊 ===== LIVE PARSING =====");
            log.warn("🏆 tournamentId = {}", parsed.getTournamentId());
            log.warn("🔗 link = {}", pn.getLink());
            log.warn("🏁 finished = {}", parsed.isFinished());
            log.warn("👥 players count = {}", parsed.getResults().size());

            parsed.getResults().forEach(r -> {
                log.warn("👤 {} | points={} | place={}",
                        r.getPlayer(),
                        r.getTotal(),
                        r.getPlace());
            });

            Player player = playerService.getByTelegramId(pn.getTelegramId());

            if (player == null) {
                log.warn("❌ Player not found: {}", pn.getTelegramId());
                return;
            }

            boolean found = tournamentResultService.processResults(
                    parsed.getResults(),
                    player,
                    parsed.getTournamentId(),
                    parsed.getNightBonus(),
                    parsed.isFinished()
            );

            log.info("✅ done, found={}", found);

            // 🔥 ВАЖНО: сохраняем processed
            if (parsed.isFinished()) {
                pn.setProcessed(true);
                notificationRepo.save(pn); // 💥 ВОТ ЭТОГО НЕ ХВАТАЛО
                log.warn("✅ PROCESSED SAVED: {}", pn.getTournamentId());
            }

        } catch (Exception e) {
            log.error("❌ Error: {}", pn.getLink(), e);
        }
    }
}