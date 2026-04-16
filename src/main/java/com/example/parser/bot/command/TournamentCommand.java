package com.example.parser.bot.command;

import com.example.parser.bot.handler.AdminHandler;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.tournament.calendar.domain.CalendarState;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Order(3)
public class TournamentCommand implements CommandHandler {

    private final AdminHandler adminHandler;


    @Override
    public boolean supports(String text, Player player) {
        return "📅 Мои турниры".equals(text);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) {

        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        adminHandler.openCalendar(chatId, telegramId, CalendarState.TOURNAMENTS, bot);

    }
}