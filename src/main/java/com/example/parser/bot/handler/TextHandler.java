package com.example.parser.bot.handler;

import com.example.parser.tournament.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@Component
@RequiredArgsConstructor
public class TextHandler {

    private final CalendarService calendarService;
    private final AdminHandler adminHandler;

    public boolean handle(Long chatId, String text, TelegramLongPollingBot bot) throws Exception {

        String state = calendarService.getState(chatId);

        if ("SEARCH_PLAYER".equals(state)) {
            adminHandler.search(chatId, text, bot);
            calendarService.reset(chatId);
            return true;
        }

        return false;
    }
}