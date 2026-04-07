package com.example.parser.bot.command;

import com.example.parser.bot.handler.AdminHandler;
import com.example.parser.notification.MessageService;
import com.example.parser.player.Player;
import com.example.parser.tournament.calendar.CalendarState;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Order(3)
public class SumCommand implements CommandHandler {

    private final AdminHandler adminHandler;
    private final MessageService messageService;

    @Override
    public boolean supports(String text, Player player) {
        return "💰 Сумма за период".equals(text);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) {

        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

//        adminHandler.openCalendar(chatId, telegramId, CalendarState.SUM, bot);// старый календарь
        messageService.sendWebAppCalendar(bot, chatId, CalendarState.SUM);
    }
}