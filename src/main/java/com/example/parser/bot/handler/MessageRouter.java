package com.example.parser.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class MessageRouter {

    private final StartHandler startHandler;
    private final RegisterHandler registerHandler;
    private final TournamentHandler tournamentHandler;
    private final HistoryHandler historyHandler;

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        if (!(update.hasMessage() && update.getMessage().hasText())) return;

        String text = update.getMessage().getText();

        if (text.equals("/start")) {
            startHandler.handle(update, bot);
            return;
        }

        if (text.equals("📅 Мои турниры") || isDateRange(text)) {
            historyHandler.handle(update, bot);
            return;
        }

        if (text.equals("💰 Сумма за период") || isDateRange(text)) {
            historyHandler.handleSum(update, bot);
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