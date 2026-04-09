package com.example.parser.bot.CallBackInline;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface CallBackAction {

    boolean support(String data);

    void handle(Update update, TelegramLongPollingBot bot) throws Exception;
}
