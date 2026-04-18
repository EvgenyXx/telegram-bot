package com.example.parser.bot.callback;

import com.example.parser.modules.notification.service.MessageService;
import com.example.parser.modules.tournament.calendar.domain.CalendarSession;
import com.example.parser.modules.tournament.calendar.service.CalendarSessionService;
import com.example.parser.modules.tournament.calendar.domain.CalendarState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class AdjustSumCallback implements ActionCallback {

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

        String[] parts = data.replace(PREFIX, "").split("_");

        Long playerId = Long.parseLong(parts[0]);
        Long tournamentId = Long.parseLong(parts[1]);

        // сохраняем состояние
        CalendarSession session = sessionService.get(chatId);
        session.setPlayerId(playerId);
        session.setState(CalendarState.ENTER_SUM); // 👈 НОВОЕ СОСТОЯНИЕ
        session.setTournamentId(tournamentId);

        //todo logs
        messageService.send(bot, chatId, "💰 Введи новую сумму:");
    }
}