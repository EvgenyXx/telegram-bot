package com.example.parser.bot.handler;

import com.example.parser.tournament.calendar.CalendarSession;
import com.example.parser.tournament.calendar.CalendarSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@Component
@RequiredArgsConstructor
public class TextHandler {

    private final CalendarSessionService sessionService;
    private final AdminHandler adminHandler;


    public boolean handle(Long chatId, String text, TelegramLongPollingBot bot) throws Exception {

        CalendarSession session = sessionService.get(chatId);

        if ("SEARCH_PLAYER".equals(session.getState())) {
            adminHandler.search(chatId, text, bot);
            sessionService.remove(chatId);
            return true;
        }

        return false;
    }
}