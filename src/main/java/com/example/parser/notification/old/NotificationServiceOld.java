package com.example.parser.notification.old;

import com.example.parser.bot.BotHolder;
import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.notification.MessageService;
import com.example.parser.notification.PlayerNotificationRepository;
import com.example.parser.player.Player;
import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.notification.formatter.TournamentMessageBuilder;
import com.example.parser.player.PlayerService;
import com.example.parser.tournament.UpcomingTournamentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.time.LocalDate;
import java.util.List;

//@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceOld {

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
        List<TournamentDto> tournaments =
                tournamentService.findPlayerTournaments(fullName);

        if (tournaments.isEmpty()) {
            return;
        }

        StringBuilder msg = new StringBuilder();
        boolean hasNew = false;

        for (TournamentDto t : tournaments) {

            boolean alreadySent =
                    notificationRepo.existsByTelegramIdAndTournamentId(
                            telegramId, t.getId());

            if (alreadySent) {
                continue;
            }

            hasNew = true;

            msg.append(messageBuilder.build(t));

            PlayerNotification pn = buildNotification(telegramId, t);
            notificationRepo.save(pn);
        }

        if (!hasNew) {
            return;
        }

        messageService.send(bot, telegramId,
                "🔥 Новые турниры:\n\n" + msg);
    }

    private static @NonNull PlayerNotification buildNotification(
            Long telegramId,
            TournamentDto t
    ) {
        LocalDate tournamentDate = null;
        String time = null;

        if (t.getDate() != null && t.getDate().getDate() != null) {
            String raw = t.getDate().getDate();

            if (raw.length() >= 10) {
                tournamentDate = LocalDate.parse(raw.substring(0, 10));
            }

            if (raw.length() >= 16) {
                time = raw.substring(11, 16);
            }
        }

        PlayerNotification pn = new PlayerNotification();
        pn.setTelegramId(telegramId);
        pn.setTournamentId(t.getId());
        pn.setLink(t.getLink());
        pn.setDate(tournamentDate);
        pn.setTime(time); // 🔥 ключевая строка


        return pn;
    }

    public void sendTournamentStarted(PlayerNotification pn) {

        TelegramLongPollingBot bot = botHolder.getBot();
        if (bot == null) {
            return;
        }

        String msg =
                "🚀 Турнир начался!\n\n" +
                        "📅 " + pn.getDate() + "\n" +
                        "🕒 " + pn.getTime() + "\n" +
                        "🔗 " + pn.getLink();

        messageService.send(bot, pn.getTelegramId(), msg);

        log.info("📩 Tournament start notification sent: tournamentId={}",
                pn.getTournamentId());
    }


    /**
     * Сервис отвечает ИСКЛЮЧИТЕЛЬНО за отправку уведомлений пользователю.
     *
     * ВАЖНО:
     * - не содержит бизнес-логики
     * - не работает с базой данных
     * - не принимает решений (когда и что отправлять)
     *
     * Используется другими сервисами и scheduler'ами:
     * - TournamentDiscoveryService → отправка новых турниров
     * - ReminderScheduler → напоминание за час
     * - TournamentStartScheduler → уведомление о начале турнира
     *
     * Здесь только формирование текста и отправка сообщений через бота.
     */

    /**
     * NORMALIZE →
     * NORMALIZED →
     * MATCH COUNT →
     * IS SAME PLAYER →
     * COMPARE →
     * ITERATE RESULT →
     * PLAYER → (каждого игрока)
     * MATCH → (каждый матч)
     * HTML SIZE
     */
}