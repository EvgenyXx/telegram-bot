package com.example.parser.config;

import com.example.parser.bot.routing.MessageRouter;
import com.example.parser.bot.transport.ParserBot;
import com.example.parser.modules.shared.exception.BotExceptionHandler;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

@Configuration
public class BotConfig {

    private final ParserBot parserBot;

    public BotConfig(MessageRouter router,
                     BotHolder botHolder,
                     BotExceptionHandler exceptionHandler,
                     @Value("${bot.token}") String token) {

        // 🔐 прокси авторизация
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        "jakfihve",
                        "ki9hfpklgrm9".toCharArray()
                );
            }
        });

        // 🌐 прокси
        DefaultBotOptions options = new DefaultBotOptions();
        options.setProxyHost("31.59.20.176");
        options.setProxyPort(6754);
        options.setProxyType(DefaultBotOptions.ProxyType.HTTP);

        // ✅ СОЗДАЁМ БОТА ВРУЧНУЮ
        this.parserBot = new ParserBot(
                options,
                router,
                botHolder,
                exceptionHandler,
                token
        );
    }

    @PostConstruct
    public void init() throws Exception {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(parserBot);
    }
}