package com.example.parser.notification;

import com.example.parser.domain.dto.ResultDto;
import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import com.example.parser.tournament.ResultService;
import com.example.parser.tournament.TournamentResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

//Чем занимается:
//🏁 Проверяет завершился ли турнир → считает результаты → отправляет итог игроку
@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentFinishScheduler {

    private final PlayerNotificationRepository repo;
    private final TournamentResultService tournamentResultService;
    private final NotificationService notificationService;
    private final PlayerService playerService;
    private final ResultService resultService;

    @Scheduled(fixedRate = 420000) // ✅ 7 минут
    public void checkFinished() {

        List<PlayerNotification> list = repo.findByFinishedFalse();

        for (PlayerNotification pn : list) {
            try {
                if (pn.getLink() == null) continue;

                // ✅ 1. НЕ будущее
                if (pn.getDate() != null && pn.getDate().isAfter(LocalDate.now())) {
                    continue;
                }

                // ✅ 2. ДОВЕРЯЕМ БАЗЕ (только стартовавшие)
                if (!Boolean.TRUE.equals(pn.getStarted())) {
                    continue;
                }

                log.info("⏱ {} | pnDate={} | now={} | started={} | finished={}",
                        pn.getTournamentId(),
                        pn.getDate(),
                        LocalDate.now(),
                        pn.getStarted(),
                        pn.getFinished()
                );

                // 👉 только теперь есть смысл парсить
                ResultService.ParsedResult parsed =
                        resultService.calculateAll(pn.getLink());

                // ❗ ещё не закончился
                if (!parsed.isFinished()) {
                    continue;
                }

                Player player = playerService.getByTelegramId(pn.getTelegramId());
                if (player == null) continue;

                boolean found = tournamentResultService.processResults(
                        parsed.getResults(),
                        player,
                        parsed.getTournamentId(),
                        parsed.getNightBonus(),
                        true
                );

                if (!found) continue;

                String msg = buildFinishMessage(parsed.getResults());
                notificationService.send(pn.getTelegramId(), msg);

                // ✅ фиксируем факт завершения
                pn.setFinished(true);
                repo.save(pn);

                log.info("🏁 Tournament finished: {}", pn.getTournamentId());

            } catch (Exception e) {
                log.error("❌ ERROR {}", pn.getLink(), e);
            }
        }
    }

    private String buildFinishMessage(List<ResultDto> results) {
        StringBuilder msg = new StringBuilder();

        msg.append("🏆 Результаты турнира:\n");
        msg.append("📅 ").append(formatDate(results)).append("\n\n");

        int limit = Math.min(results.size(), 10);

        for (int i = 0; i < limit; i++) {
            ResultDto r = results.get(i);
            msg.append(i + 1)
                    .append(". ")
                    .append(r.getPlayer())
                    .append(" — ")
                    .append(r.getTotal())
                    .append("\n");
        }

        return msg.toString();
    }

    private String formatDate(List<ResultDto> results) {
        if (results.isEmpty() || results.get(0).getDate() == null) {
            return "";
        }
        return results.get(0).getDate();
    }
}