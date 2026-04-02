package com.example.parser.bot.handler;

import com.example.parser.bot.AdminMenuService;
import com.example.parser.tournament.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class AdminHandler {

    private final AdminMenuService adminMenuService;
    private final CalendarService calendarService;

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        if (text.equals("📊 Статистика")) {
            adminMenuService.showPlayers(chatId, bot);
        }
    }

    public boolean isInProgress(Long chatId) {
        return calendarService.isInProgress(chatId);
    }

    public void handlePlayerSelected(Long chatId, Long playerId, TelegramLongPollingBot bot) throws Exception {
        adminMenuService.handlePlayerSelected(chatId, playerId, bot);
    }

    // 🔥 ОТКРЫТИЕ КАЛЕНДАРЯ
    public void openCalendar(Long chatId, Long telegramId, String state, TelegramLongPollingBot bot) {
        calendarService.setState(chatId, state);
        calendarService.open(chatId, telegramId, bot);
    }

    // 🔥 CALLBACK КАЛЕНДАРЯ
    public void handleCalendarCallback(Long chatId, String data, TelegramLongPollingBot bot) {
        calendarService.handleCallback(chatId, data, bot);
    }

    // 🔥 ВОТ ЭТОГО ТЕБЕ НЕ ХВАТАЛО
    public void reset(Long chatId) {
        calendarService.reset(chatId);
    }
}