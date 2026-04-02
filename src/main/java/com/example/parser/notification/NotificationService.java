package com.example.parser.notification;

import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.domain.entity.Player;
import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.notification.formatter.TournamentMessageBuilder;
import com.example.parser.service.MessageService;
import com.example.parser.player.PlayerService;
import com.example.parser.tournament.UpcomingTournamentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final PlayerService userService;
    private final UpcomingTournamentService tournamentService;
    private final MessageService messageService;
    private final PlayerNotificationRepository notificationRepo;
    private final TournamentMessageBuilder messageBuilder;

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
                            telegramId, t.getId());

            if (alreadySent) {
                log.debug("Skip already sent tournamentId={}", t.getId());
                continue;
            }

            hasNew = true;

            // 📦 используем builder
            msg.append(messageBuilder.build(t));

            // 📅 сохраняем дату (это остаётся здесь — это бизнес-логика)
            LocalDate tournamentDate = null;

            if (t.getDate() != null && t.getDate().getDate() != null) {
                String raw = t.getDate().getDate();
                if (raw.length() >= 10) {
                    tournamentDate = LocalDate.parse(raw.substring(0, 10));
                }
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

        messageService.send(bot, telegramId,
                "🔥 Новые турниры:\n\n" + msg);

        log.info("Notification sent to telegramId={}", telegramId);
    }
}