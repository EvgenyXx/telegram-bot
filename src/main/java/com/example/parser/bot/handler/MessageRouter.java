package com.example.parser.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
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

    private boolean isAdmin(Long chatId) {
        return ADMIN_ID.equals(chatId);
    }

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        if (!(update.hasMessage() && update.getMessage().hasText())) return;

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        // 👉 вход в админку
        if (text.equals("📊 Статистика") && isAdmin(chatId)) {
            adminHandler.handle(update, bot);
            return;
        }

        // 👉 если админ уже внутри сценария
        if (isAdmin(chatId) && adminHandler.isInProgress(chatId)) {
            adminHandler.handle(update, bot);
            return;
        }

        // 👇 обычная логика
        if (text.equals("/start")) {
            startHandler.handle(update, bot);
            return;
        }

        if (text.equals("📅 Мои турниры")) {
            userState.put(chatId, "LIST");
            historyHandler.handle(update, bot);
            return;
        }

        if (text.equals("💰 Сумма за период")) {
            userState.put(chatId, "SUM");
            historyHandler.handleSum(update, bot);
            return;
        }

        if (isDateRange(text)) {
            String state = userState.get(chatId);

            if ("SUM".equals(state)) {
                historyHandler.handleSum(update, bot);
            } else {
                historyHandler.handle(update, bot);
            }
            return;
        }

        if (text.startsWith("http")) {
            tournamentHandler.handle(update, bot);
            return;
        }

        registerHandler.handle(update, bot);
    }

    private boolean isDateRange(String text) {
        return text.matches("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}\\.\\d{2}\\.\\d{4}");
    }
}