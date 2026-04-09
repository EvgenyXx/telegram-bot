package com.example.parser.bot.handler;

import com.example.parser.tournament.calendar.CalendarSession;
import com.example.parser.tournament.calendar.CalendarSessionService;
import com.example.parser.tournament.calendar.CalendarState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@Component
@RequiredArgsConstructor
public class TextHandler {//todo не нравится если что переделать

    private final CalendarSessionService sessionService;
    private final AdminHandler adminHandler;

    public boolean handle(Long chatId, String text, TelegramLongPollingBot bot) throws Exception {

        CalendarSession session = sessionService.get(chatId);

        if (session == null) {
            return false;
        }

        // 🔍 Поиск игрока
        if (session.getState() == CalendarState.SEARCH_PLAYER) {
            adminHandler.search(chatId, text, bot);
            sessionService.remove(chatId);
            return true;
        }

        // 💰 Ввод суммы
        if (session.getState() == CalendarState.ENTER_SUM) {
            try {
                Long sum = Long.parseLong(text);

                adminHandler.updateSum(
                        chatId,
                        session.getTournamentId(), // 👈 ВАЖНО
                        session.getPlayerId(),
                        sum,
                        bot
                );

                sessionService.remove(chatId);

            } catch (NumberFormatException e) {
                // ❗ лучше дать пользователю шанс ввести ещё раз
                return true;
            }
            return true;
        }

        return false;
    }
}