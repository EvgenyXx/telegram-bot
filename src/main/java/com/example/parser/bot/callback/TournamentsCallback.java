package com.example.parser.bot.callback;

import com.example.parser.bot.handler.AdminHandler;
import com.example.parser.modules.tournament.calendar.domain.CalendarState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
public class TournamentsCallback implements ActionCallback {

    private final AdminHandler adminHandler;

    private static final String TOURNAMENTS = "tournaments";

    @Override
    public boolean support(String data) {
        return TOURNAMENTS.equals(data);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long telegramId = update.getCallbackQuery().getFrom().getId();
        String data = update.getCallbackQuery().getData();

        log.debug("Tournaments callback: chatId={}, telegramId={}, data={}", chatId, telegramId, data);

        adminHandler.openCalendar(chatId, telegramId, CalendarState.TOURNAMENTS, bot);

        log.debug("Calendar opened (TOURNAMENTS): chatId={}, telegramId={}", chatId, telegramId);
    }
}