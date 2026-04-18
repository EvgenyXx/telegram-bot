package com.example.parser.bot.handler;

import com.example.parser.modules.player.domain.Player;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface CommandHandler {
    boolean supports(String text, Player player);
    void handle(Update update, TelegramLongPollingBot bot) throws Exception;
}