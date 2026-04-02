package com.example.parser.service;

import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.domain.entity.Player;
import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.repository.PlayerNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j //todo разбить на маленькие классы
public class NotificationService {

    private final PlayerService userService;
    private final UpcomingTournamentService tournamentService;
    private final MessageService messageService;
    private final PlayerNotificationRepository notificationRepo;

    public void notifyUser(Long telegramId, TelegramLongPollingBot bot) {

        Player user = userService.getByTelegramId(telegramId);

        if (user == null) {
            log.warn("User not found for telegramId={}", telegramId);
            return;
        }

        String fullName = user.getName();
        log.info("CHECK user: [{}]", fullName);

        List<TournamentDto> tournaments =
                tournamentService.findPlayerTournaments(fullName);

        if (tournaments.isEmpty()) {
            log.info("No tournaments found for [{}]", fullName);
            return;
        }

        StringBuilder msg = new StringBuilder();
        boolean hasNew = false;

        for (TournamentDto t : tournaments) {

            boolean alreadySent =
                    notificationRepo.existsByTelegramIdAndTournamentId(
                            telegramId, t.getId()
                    );

            if (alreadySent) {
                log.debug("Skip already sent tournamentId={}", t.getId());
                continue;
            }

            hasNew = true;

            // 💥 парсим дату
            LocalDate tournamentDate = null;
            String dateStr = "-";
            String timeStr = "-";

            if (t.getDate() != null && t.getDate().getDate() != null) {
                String raw = t.getDate().getDate();

                if (raw.length() >= 16) {
                    dateStr = raw.substring(0, 10);
                    timeStr = raw.substring(11, 16);
                    tournamentDate = LocalDate.parse(dateStr);
                }
            }

            log.info("New tournament found: id={}, league={}, date={}",
                    t.getId(), t.getLeague(), dateStr);

            // 💥 сообщение
            msg.append("📅 ").append(dateStr).append(" ").append(timeStr).append("\n");
            msg.append("🏆 ").append(nullSafe(t.getLeague())).append("\n");
            msg.append("📍 ").append(nullSafe(t.getHall())).append("\n\n");
            msg.append("🔗 ").append(t.getLink()).append("\n\n");

            if (t.getPlayers() != null && !t.getPlayers().isEmpty()) {
                msg.append("👥 Участники:\n");

                for (String p : t.getPlayers()) {
                    if (p.equalsIgnoreCase(fullName)) {
                        msg.append("👉 ").append(p).append(" (ты)\n");
                    } else {
                        msg.append("• ").append(p).append("\n");
                    }
                }

                msg.append("\n");
            }

            // 💾 сохраняем
            PlayerNotification pn = new PlayerNotification();
            pn.setTelegramId(telegramId);
            pn.setTournamentId(t.getId());
            pn.setLink(t.getLink());
            pn.setDate(tournamentDate);
            pn.setProcessed(false);

            notificationRepo.save(pn);

            log.debug("Saved notification: tournamentId={}, link={}",
                    t.getId(), t.getLink());
        }

        if (!hasNew) {
            log.info("No new tournaments for [{}]", fullName);
            return;
        }

        messageService.send(
                bot,
                telegramId,
                "🔥 Новые турниры:\n\n" + msg
        );

        log.info("Notification sent to telegramId={}", telegramId);
    }

    private String nullSafe(String val) {
        return val == null ? "-" : val;
    }
}