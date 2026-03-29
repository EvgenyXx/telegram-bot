package com.example.parser.bot.handler;

import com.example.parser.formatter.StatsFormatter;
import com.example.parser.dto.FullStatsDto;
import com.example.parser.entity.Player;
import com.example.parser.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageRouter {

    private final StartHandler startHandler;
    private final RegisterHandler registerHandler;
    private final TournamentHandler tournamentHandler;
    private final HistoryHandler historyHandler;
    private final AdminHandler adminHandler;
    private final MessageService messageService;
    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;
    private final StatsFormatter statsFormatter;

    private static final List<Long> ADMINS = List.of(
            459307336L, 1632772141L, 5429880868L
    );

    private boolean isAdmin(Long telegramId) {
        return ADMINS.contains(telegramId);
    }

    private boolean isBlocked(Player player, Long chatId, TelegramLongPollingBot bot) {
        if (player != null && player.isBlocked()) {
            messageService.send(bot, chatId, "🚫 Ты заблокирован");
            return true;
        }
        return false;
    }

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        // ================= CALLBACK =================
        if (update.hasCallbackQuery()) {

            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Long telegramId = update.getCallbackQuery().getFrom().getId();

            bot.execute(new AnswerCallbackQuery(update.getCallbackQuery().getId()));

            Player player = playerService.getByTelegramId(telegramId);

            // 🔥 БЛОКИРОВКА
            if (isBlocked(player, chatId, bot)) return;

            String data = update.getCallbackQuery().getData();

            if (data.startsWith("date_") || data.startsWith("month_") || data.equals("ignore")) {
                adminHandler.handleCalendarCallback(chatId, data, bot);
                return;
            }

            if (data.startsWith("player_")) {
                Long playerId = Long.parseLong(data.replace("player_", ""));
                adminHandler.handlePlayerSelected(chatId, playerId, bot);
                return;
            }

            if (data.startsWith("block_user_")) {
                Long playerId = Long.parseLong(data.replace("block_user_", ""));
                playerService.block(playerId);
                messageService.send(bot, chatId, "🚫 Пользователь заблокирован");
                adminHandler.handlePlayerSelected(chatId, playerId, bot);
                return;
            }

            if (data.startsWith("unblock_user_")) {
                Long playerId = Long.parseLong(data.replace("unblock_user_", ""));
                playerService.unblock(playerId);
                messageService.send(bot, chatId, "✅ Пользователь разблокирован");
                adminHandler.handlePlayerSelected(chatId, playerId, bot);
                return;
            }

            if (data.equals("tournaments")) {
                adminHandler.openCalendar(chatId, telegramId, "PLAYER_TOURNAMENTS", bot);
                return;
            }

            if (data.equals("sum")) {
                adminHandler.openCalendar(chatId, telegramId, "PLAYER_SUM", bot);
                return;
            }

            return;
        }

        // ================= TEXT =================
        if (!(update.hasMessage() && update.getMessage().hasText())) return;

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        Player player = playerService.getByTelegramId(telegramId);

        // 🔥 БЛОКИРОВКА
        if (isBlocked(player, chatId, bot)) return;

        // 👉 админ
        if (text.equals("📊 Статистика") && isAdmin(telegramId)) {
            adminHandler.handle(update, bot);
            return;
        }

        if (isAdmin(telegramId) && adminHandler.isInProgress(chatId)) {
            adminHandler.handle(update, bot);
            return;
        }

        // 👉 пользователь
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
            String response = statsFormatter.formatFullStats(stats);
            messageService.send(bot, chatId, response);
            return;
        }

        if (text.equals("/start")) {
            if (player == null) {
                startHandler.handle(update, bot);
            } else {
                messageService.send(bot, chatId, "С возвращением, " + player.getName());
                messageService.sendMenu(bot, chatId, telegramId);
            }
            return;
        }

        if (text.startsWith("http")) {
            tournamentHandler.handle(update, bot);
            return;
        }

        if (player == null) {
            registerHandler.handle(update, bot);
        } else {
            messageService.send(bot, chatId, "Неизвестная команда 🤷‍♂️");
            messageService.sendMenu(bot, chatId, telegramId);
        }
    }
}