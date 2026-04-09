package com.example.parser.bot.CallBackInline;

import com.example.parser.notification.MessageService;
import com.example.parser.tournament.calendar.CalendarSession;
import com.example.parser.tournament.calendar.CalendarSessionService;
import com.example.parser.tournament.calendar.CalendarState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class AdjustSumCallback implements CallBackAction {

    private static final String PREFIX = "adjust_sum_";

    private final CalendarSessionService sessionService;
    private final MessageService messageService;

    @Override
    public boolean support(String data) {
        return data.startsWith(PREFIX);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();

        Long playerId = Long.parseLong(data.replace(PREFIX, ""));

        // сохраняем состояние
        CalendarSession session = sessionService.get(chatId);
        session.setPlayerId(playerId);
        session.setState(CalendarState.ENTER_SUM); // 👈 НОВОЕ СОСТОЯНИЕ

        //todo logs
        messageService.send(bot, chatId, "💰 Введи новую сумму:");
    }
}