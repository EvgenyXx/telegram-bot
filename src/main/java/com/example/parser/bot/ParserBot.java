package com.example.parser.bot;

import com.example.parser.bot.handler.MessageRouter;
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
        try {
            router.handle(update, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void init() {
        botHolder.setBot(this);
    }

}