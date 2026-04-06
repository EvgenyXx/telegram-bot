package com.example.parser.bot.command;

import com.example.parser.player.Player;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface CommandHandler {
    boolean supports(String text, Player player);
    void handle(Update update, TelegramLongPollingBot bot) throws Exception;
}