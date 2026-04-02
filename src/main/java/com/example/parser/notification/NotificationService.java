package com.example.parser.notification;

import com.example.parser.bot.BotHolder;
import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.player.Player;
import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.notification.formatter.TournamentMessageBuilder;
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
    private final BotHolder botHolder;

    public void notifyUser(Long telegramId) {

        TelegramLongPollingBot bot = botHolder.getBot();
        if (bot == null) {
            log.warn("❌ Bot is not initialized yet");
            return;
        }

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

            // 📦 билд сообщения
            msg.append(messageBuilder.build(t));

            // 📅 дата
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

    public void sendTournamentStarted(PlayerNotification pn) {

        TelegramLongPollingBot bot = botHolder.getBot();
        if (bot == null) {
            log.warn("❌ Bot is not initialized yet");
            return;
        }

        Long chatId = pn.getTelegramId();

        String msg =
                "🚀 Турнир начался!\n\n" +
                        "📅 " + pn.getDate() + "\n" +
                        "🕒 " + pn.getTime() + "\n" +
                        "🔗 " + pn.getLink();

        messageService.send(bot, chatId, msg);

        log.info("📩 Tournament start notification sent: tournamentId={}",
                pn.getTournamentId());
    }
}