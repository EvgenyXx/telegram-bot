package com.example.parser.bot.CallBackInline;

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
public class CallBackSum implements CallBackAction {

    private final AdminHandler adminHandler;

    private static final String SUM = "sum";

    @Override
    public boolean support(String data) {
        return SUM.equals(data);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        Long telegramId = update.getCallbackQuery().getFrom().getId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();

        log.debug("Sum callback: chatId={}, telegramId={}, data={}", chatId, telegramId, data);

        adminHandler.openCalendar(chatId, telegramId, CalendarState.SUM, bot);

        log.debug("Calendar opened (SUM): chatId={}, telegramId={}", chatId, telegramId);
    }
}