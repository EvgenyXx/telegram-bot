package com.example.parser.bot.handler;

import com.example.parser.bot.AdminMenuService;
import com.example.parser.notification.MessageService;
import com.example.parser.tournament.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@Component
@RequiredArgsConstructor
public class AdminHandler {

    private final AdminMenuService adminMenuService;
    private final CalendarService calendarService;
    private final MessageService messageService;

    public boolean isInProgress(Long chatId) {
        return calendarService.isInProgress(chatId);
    }

    public void handlePlayerSelected(Long chatId, Long playerId, TelegramLongPollingBot bot) throws Exception {
        adminMenuService.handlePlayerSelected(chatId, playerId, bot);
    }

    public void openCalendar(Long chatId, Long telegramId, String state, TelegramLongPollingBot bot) {
        calendarService.setState(chatId, state);
        calendarService.open(chatId, telegramId, bot);
    }

    public void handleCalendarCallback(Long chatId, String data, TelegramLongPollingBot bot) {
        calendarService.handleCallback(chatId, data, bot);
    }

    public void reset(Long chatId) {
        calendarService.reset(chatId);
    }

    // 🔥 ПОИСК
    public void search(Long chatId, String query, TelegramLongPollingBot bot) throws Exception {
        adminMenuService.searchWithPagination(chatId, query, 0, bot);
    }

    public void searchPage(Long chatId, String query, int page, TelegramLongPollingBot bot) throws Exception {
        adminMenuService.searchWithPagination(chatId, query, page, bot);
    }
}