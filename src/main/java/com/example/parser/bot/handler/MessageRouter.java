package com.example.parser.bot.handler;

import com.example.parser.bot.command.CommandRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class MessageRouter {

    private final CallbackHandler callbackHandler;
    private final CommandRouter commandRouter;
    private final TextHandler textHandler;

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        if (update.hasCallbackQuery()) {
            callbackHandler.handle(update, bot);
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {

            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // 🔥 сначала state
            if (textHandler.handle(chatId, text, bot)) {
                return;
            }

            // 🔥 потом команды
            commandRouter.handle(update, bot);
        }
    }
}