package com.example.parser.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class MessageRouter {

    private final CallbackHandler callbackHandler;
    private final TextHandler textHandler;

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        if (update.hasCallbackQuery()) {
            callbackHandler.handle(update, bot);
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            textHandler.handle(update, bot);
        }
    }
}