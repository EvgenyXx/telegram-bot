package com.example.parser.bot.handler;

import com.example.parser.entity.Player;
import com.example.parser.service.PlayerService;
import com.example.parser.service.TournamentResultService;
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
    private final com.example.parser.service.MessageService messageService;
    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;

    private static final List<Long> ADMINS = List.of(
            459307336L,
            1632772141L,
            5429880868L
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

            if (data.equals("tournaments")) {
                adminHandler.userState.put(chatId, "PLAYER_TOURNAMENTS");
                adminHandler.openCalendar(chatId, bot);
                return;
            }

            if (data.equals("sum")) {
                adminHandler.userState.put(chatId, "PLAYER_SUM");
                adminHandler.openCalendar(chatId, bot);
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

        // 👉 пользователь
        if (text.equals("📅 Мои турниры")) {
            adminHandler.userState.put(chatId, "USER_TOURNAMENTS");
            adminHandler.openCalendar(chatId, bot);
            return;
        }

        if (text.equals("💰 Сумма за период")) {
            adminHandler.userState.put(chatId, "USER_SUM");
            adminHandler.openCalendar(chatId, bot);
            return;
        }

        if (text.equals("📊 Моя статистика")) {
            Player player = playerService.getByTelegramId(telegramId);
            String response = tournamentResultService.getFullStats(player);
            messageService.send(bot, chatId, response);
            return;
        }

        if (text.equals("/start")) {
            startHandler.handle(update, bot);
            messageService.sendMenu(bot, chatId, telegramId); // ✅ ВАЖНО
            return;
        }

        if (text.startsWith("http")) {
            tournamentHandler.handle(update, bot);
            return;
        }

        registerHandler.handle(update, bot);
        messageService.sendMenu(bot, chatId, telegramId); // ✅ после регистрации
    }
}