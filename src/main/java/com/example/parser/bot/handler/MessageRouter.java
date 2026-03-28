package com.example.parser.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MessageRouter {

    private final StartHandler startHandler;
    private final RegisterHandler registerHandler;
    private final TournamentHandler tournamentHandler;
    private final HistoryHandler historyHandler;
    private final AdminHandler adminHandler;

    private final Map<Long, String> userState = new HashMap<>();

    private static final Long ADMIN_ID = 459307336L;

    private boolean isAdmin(Long telegramId) {
        return ADMIN_ID.equals(telegramId);
    }

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        // 👉 CALLBACK
        if (update.hasCallbackQuery()) {
            bot.execute(new AnswerCallbackQuery(update.getCallbackQuery().getId()));

            String data = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Long telegramId = update.getCallbackQuery().getFrom().getId(); // 🔥

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
                userState.put(telegramId, "PLAYER_TOURNAMENTS");
                adminHandler.openCalendar(chatId, bot);
                return;
            }

            if (data.equals("sum")) {
                userState.put(telegramId, "PLAYER_SUM");
                adminHandler.openCalendar(chatId, bot);
                return;
            }

            return;
        }

        // 👉 TEXT
        if (!(update.hasMessage() && update.getMessage().hasText())) return;

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId(); // 🔥

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
            userState.put(telegramId, "USER_TOURNAMENTS");
            adminHandler.openCalendar(chatId, bot);
            return;
        }

        if (text.equals("💰 Сумма за период")) {
            userState.put(telegramId, "USER_SUM");
            adminHandler.openCalendar(chatId, bot);
            return;
        }

        if (text.equals("/start")) {
            startHandler.handle(update, bot);
            return;
        }

        if (text.startsWith("http")) {
            tournamentHandler.handle(update, bot);
            return;
        }

        registerHandler.handle(update, bot);
    }
}