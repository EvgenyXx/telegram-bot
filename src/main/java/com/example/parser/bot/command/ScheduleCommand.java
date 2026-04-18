package com.example.parser.bot.command;

import com.example.parser.bot.handler.CommandHandler;
import com.example.parser.modules.player.domain.Player;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;


//todo непонятный класс возможно удаление
public class ScheduleCommand implements CommandHandler {

    @Override
    public boolean supports(String text, Player player) {
        return text.contains("Список турниров");
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

    }
}
