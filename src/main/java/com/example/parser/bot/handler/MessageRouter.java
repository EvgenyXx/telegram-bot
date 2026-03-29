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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

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

        // ===== CALLBACK =====
        if (update.hasCallbackQuery()) {
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Long telegramId = update.getCallbackQuery().getFrom().getId();

            bot.execute(new AnswerCallbackQuery(update.getCallbackQuery().getId()));

            Player player = playerService.getByTelegramId(telegramId);
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

        // ===== TEXT =====
        if (!(update.hasMessage() && update.getMessage().hasText())) return;

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        Player player = playerService.getByTelegramId(telegramId);
        if (isBlocked(player, chatId, bot)) return;

        // 🔥 INFO ТОЛЬКО КОМАНДА
        if (text.equals("/info")) {
            InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("🌐 Открыть турниры");
            button.setUrl("https://masters-league.com/tours-rus/");

            keyboard.setKeyboard(List.of(List.of(button)));

            messageService.sendInlineKeyboard(
                    bot,
                    chatId,
                    "ℹ️ Информация о турнирах:",
                    keyboard
            );

            return;
        }

        // 🔥 ГЛАВНЫЙ ФИКС — ПЕРЕБИВАНИЕ СОСТОЯНИЯ
        if (adminHandler.isInProgress(chatId)) {

            if (text.equals("📅 Мои турниры") ||
                    text.equals("💰 Сумма за период") ||
                    text.equals("📊 Моя статистика") ||
                    text.equals("/start") ||
                    text.equals("/info")) {

                adminHandler.reset(chatId); // 👈 ключ
            } else {
                adminHandler.handle(update, bot);
                return;
            }
        }

        // ===== АДМИН =====
        if (text.equals("📊 Статистика") && isAdmin(telegramId)) {
            adminHandler.handle(update, bot);
            return;
        }

        // ===== ПОЛЬЗОВАТЕЛЬ =====

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

        // ===== START =====
        if (text.equals("/start")) {
            if (player == null) {
                startHandler.handle(update, bot);
            } else {
                messageService.send(bot, chatId, "С возвращением, " + player.getName());
                messageService.sendMenu(bot, chatId, telegramId);
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
            messageService.sendMenu(bot, chatId, telegramId);
        }
    }
}