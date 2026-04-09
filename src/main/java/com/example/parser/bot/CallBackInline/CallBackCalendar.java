package com.example.parser.bot.CallBackInline;

import com.example.parser.bot.handler.AdminHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallBackCalendar implements CallBackAction {

    private final AdminHandler adminHandler;

    private static final String DATE = "date_";
    private static final String MONTH = "month_";
    private static final String IGNORE = "ignore";

    @Override
    public boolean support(String data) {
        return data.startsWith(DATE)
                || data.startsWith(MONTH)
                || data.equals(IGNORE);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        log.debug("Calendar callback: chatId={}, data={}", chatId, data);

        adminHandler.handleCalendarCallback(chatId, data, bot);

        log.debug("Calendar handled: chatId={}", chatId);
    }
}