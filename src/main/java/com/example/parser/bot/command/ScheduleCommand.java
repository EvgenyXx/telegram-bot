package com.example.parser.bot.command;

import com.example.parser.player.Player;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;



public class ScheduleCommand implements CommandHandler{

    @Override
    public boolean supports(String text, Player player) {
        return text.contains("Список турниров");
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

    }
}
