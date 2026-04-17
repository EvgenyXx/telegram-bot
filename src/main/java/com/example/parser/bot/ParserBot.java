package com.example.parser.bot;

import com.example.parser.bot.handler.MessageRouter;
import com.example.parser.modules.shared.exception.BotExceptionHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class ParserBot extends TelegramLongPollingBot {

    private final MessageRouter router;
    private final BotHolder botHolder;
    private final BotExceptionHandler exceptionHandler;

    @Value("${bot.token}")
    private String token;

    @Override
    public String getBotUsername() {
        return "@table_tennis_parser_bot";
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = extractChatId(update);

        try {
            router.handle(update, this);
        } catch (Exception e) {

            // ❗ НЕ шлём ошибки для системных событий типа kicked
            if (update.getMyChatMember() != null) {
                // только лог
                System.err.println("Bot state error: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            // ✅ норм обработка всех остальных ошибок
            exceptionHandler.handle(e, this, chatId);
        }
    }

    private Long extractChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        }
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        if (update.getMyChatMember() != null) {
            return update.getMyChatMember().getChat().getId();
        }
        return null;
    }



    @PostConstruct
    public void init() {
        botHolder.setBot(this);
    }



}