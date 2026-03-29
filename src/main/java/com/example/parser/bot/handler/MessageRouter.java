package com.example.parser.bot.handler;

import com.example.parser.StatsFormatter;
import com.example.parser.dto.FullStatsDto;
import com.example.parser.entity.Player;
import com.example.parser.service.MessageService;
import com.example.parser.service.PlayerService;
import com.example.parser.service.TournamentResultService;
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

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        // 👉 CALLBACK
        if (update.hasCallbackQuery()) {
            bot.execute(new AnswerCallbackQuery(update.getCallbackQuery().getId()));

            String data = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (data.startsWith("date_") || data.startsWith("month_") || data.equals("ignore")) {
                adminHandler.handleCalendarCallback(chatId, data, bot);
                return;
            }

            if (data.startsWith("player_")) {
                Long playerId = Long.parseLong(data.replace("player_", ""));
                adminHandler.handlePlayerSelected(chatId, playerId, bot);
                return;
            }

            // 🔥 ВОТ ТУТ ФИКС
            if (data.equals("tournaments")) {
                adminHandler.openCalendar(chatId, "PLAYER_TOURNAMENTS", bot);
                return;
            }

            if (data.equals("sum")) {
                adminHandler.openCalendar(chatId, "PLAYER_SUM", bot);
                return;
            }

            return;
        }

        // 👉 TEXT
        if (!(update.hasMessage() && update.getMessage().hasText())) return;

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        // 👉 админ
        if (text.equals("📊 Статистика") && isAdmin(telegramId)) {
            adminHandler.handle(update, bot);
            return;
        }

        if (isAdmin(telegramId) && adminHandler.isInProgress(chatId)) {
            adminHandler.handle(update, bot);
            return;
        }

        // 🔥 ВОТ ТУТ ФИКС (пользователь)
        if (text.equals("📅 Мои турниры")) {
            adminHandler.openCalendar(chatId, "USER_TOURNAMENTS", bot);
            return;
        }

        if (text.equals("💰 Сумма за период")) {
            adminHandler.openCalendar(chatId, "USER_SUM", bot);
            return;
        }

        if (text.equals("📊 Моя статистика")) {
            Player player = playerService.getByTelegramId(telegramId);

            FullStatsDto stats = tournamentResultService.getFullStats(player);
            String response = statsFormatter.formatFullStats(stats);

            messageService.send(bot, chatId, response);
            return;
        }

        if (text.equals("/start")) {
            Player player = playerService.getByTelegramId(telegramId);

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

        if (text.equals("/info")) {

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("🌐 Открыть Masters League");
            button.setUrl("https://masters-league.com/tours-rus/");

            InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
            markup.setKeyboard(List.of(List.of(button)));

            messageService.sendInlineKeyboard(
                    bot,
                    chatId,
                    "ℹ️ Информация о турнирах",
                    markup
            );

            return;
        }

        Player player = playerService.getByTelegramId(telegramId);

        if (player == null) {
            registerHandler.handle(update, bot);
        } else {
            messageService.send(bot, chatId, "Неизвестная команда 🤷‍♂️");
            messageService.sendMenu(bot, chatId, telegramId);
        }
    }
}