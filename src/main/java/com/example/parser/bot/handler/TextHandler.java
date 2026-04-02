package com.example.parser.bot.handler;

import com.example.parser.config.AdminProperties;
import com.example.parser.domain.dto.FullStatsDto;
import com.example.parser.player.Player;
import com.example.parser.stats.StatsFormatter;
import com.example.parser.match.LiveMatchService;
import com.example.parser.notification.MessageService;
import com.example.parser.player.PlayerService;
import com.example.parser.tournament.TournamentResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class TextHandler {

    private final StartHandler startHandler;
    private final RegisterHandler registerHandler;
    private final TournamentHandler tournamentHandler;
    private final AdminHandler adminHandler;
    private final MessageService messageService;
    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;
    private final StatsFormatter statsFormatter;
    private final AdminProperties adminProperties;
    private final LiveMatchHandler liveMatchHandler;
    private final LiveMatchService liveMatchService;

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        Player player = playerService.getByTelegramId(telegramId);

        // 🚫 блок
        if (player != null && player.isBlocked()) {
            messageService.send(bot, chatId, "🚫 Ты заблокирован");
            return;
        }

        // ===== LIVE LINK =====
        if (liveMatchService.isWaiting(chatId) && text.startsWith("http")) {
            liveMatchHandler.handleLink(chatId, text, bot);
            return;
        }

        if (text.equals("🔥 Лайв матч")) {
            String link = liveMatchService.getLink(chatId);

            if (link != null) {
                liveMatchHandler.start(chatId, bot);
            } else {
                liveMatchHandler.waitForLink(chatId, bot);
            }
            return;
        }

        // ===== INFO =====
        if (text.equals("/info")) {
            liveMatchHandler.sendInfo(chatId, bot);
            return;
        }

        // ===== ADMIN STATE =====
        if (adminHandler.isInProgress(chatId)) {
            if (text.equals("📅 Мои турниры") ||
                    text.equals("💰 Сумма за период") ||
                    text.equals("📊 Моя статистика") ||
                    text.equals("/start") ||
                    text.equals("/info")) {

                adminHandler.reset(chatId);

            } else {
                adminHandler.handle(update, bot);
                return;
            }
        }

        // ===== ADMIN =====
        if (text.equals("📊 Статистика") && adminProperties.isAdmin(telegramId)) {
            adminHandler.handle(update, bot);
            return;
        }

        // ===== USER =====
        if (text.equals("📅 Мои турниры")) {
            adminHandler.openCalendar(chatId, telegramId, "USER_TOURNAMENTS", bot);
            return;
        }

        if (text.equals("💰 Сумма за период")) {
            adminHandler.openCalendar(chatId, telegramId, "USER_SUM", bot);
            return;
        }

        if (text.equals("📊 Моя статистика")) {
            if (player == null) {
                messageService.send(bot, chatId, "❌ Пользователь не найден");
                return;
            }

            FullStatsDto stats = tournamentResultService.getFullStats(player);

            messageService.send(bot, chatId,
                    statsFormatter.formatFullStats(stats));
            return;
        }

        // ===== START =====
        if (text.equals("/start")) {
            if (player == null) {
                startHandler.handle(update, bot);
            } else {
                messageService.send(bot, chatId,
                        "С возвращением, " + player.getName());

                messageService.sendMenu(bot, chatId, telegramId, null);
            }
            return;
        }

        // ===== LINK =====
        if (text.startsWith("http")) {
            tournamentHandler.handle(update, bot);
            return;
        }

        // ===== FALLBACK =====
        if (player == null) {
            registerHandler.handle(update, bot);
        } else {
            messageService.send(bot, chatId, "Неизвестная команда 🤷‍♂️");
            messageService.sendMenu(bot, chatId, telegramId, null);
        }
    }
}