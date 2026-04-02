package com.example.parser.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@Component
public class BotHolder {

    private TelegramLongPollingBot bot;

    public void setBot(TelegramLongPollingBot bot) {
        this.bot = bot;
    }

    public TelegramLongPollingBot getBot() {
        return bot;
    }
}