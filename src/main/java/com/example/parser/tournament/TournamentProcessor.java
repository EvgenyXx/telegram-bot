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

    public void process(PlayerNotification pn) {

        try {
            log.info("🚀 Start: {}", pn.getLink());

            // 💥 ВСЯ МАГИЯ ТУТ
            ResultService.ParsedResult parsed =
                    resultService.calculateAll(pn.getLink());

            // 👉 игрок
            Player player = playerService.getByTelegramId(pn.getTelegramId());
            if (player == null) {
                log.warn("❌ Player not found: {}", pn.getTelegramId());
                return;
            }

            // 👉 сохраняем
            boolean found = tournamentResultService.processResults(
                    parsed.getResults(),
                    player,
                    parsed.getTournamentId(),
                    parsed.getNightBonus(),
                    parsed.isFinished()
            );

            log.info("✅ done, found={}", found);

            // 👉 помечаем как обработанный если закончился
            if (parsed.isFinished()) {
                pn.setProcessed(true);
            }

        } catch (Exception e) {
            log.error("❌ Error: {}", pn.getLink(), e);
        }
    }
}