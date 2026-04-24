package com.example.parser.bot.transport;

import com.example.parser.bot.routing.MessageRouter;
import com.example.parser.config.BotHolder;
import com.example.parser.modules.shared.exception.BotExceptionHandler;
import jakarta.annotation.PostConstruct;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class ParserBot extends TelegramLongPollingBot {

    private final MessageRouter router;
    private final BotHolder botHolder;
    private final BotExceptionHandler exceptionHandler;
    private final String token;

    public ParserBot(DefaultBotOptions options,
                     MessageRouter router,
                     BotHolder botHolder,
                     BotExceptionHandler exceptionHandler,
                     String token) {
        super(options);
        this.router = router;
        this.botHolder = botHolder;
        this.exceptionHandler = exceptionHandler;
        this.token = token;
    }

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
            if (update.getMyChatMember() != null) {
                System.err.println("Bot state error: " + e.getMessage());
                e.printStackTrace();
                return;
            }
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