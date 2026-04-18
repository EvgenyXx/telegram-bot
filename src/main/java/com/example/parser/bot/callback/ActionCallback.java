package com.example.parser.bot.callback;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface ActionCallback {

    boolean support(String data);

    void handle(Update update, TelegramLongPollingBot bot) throws Exception;
}
