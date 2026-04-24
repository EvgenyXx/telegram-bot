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

@Configuration
public class BotConfig {

    private final ParserBot parserBot;

    public BotConfig(MessageRouter router,
                     BotHolder botHolder,
                     BotExceptionHandler exceptionHandler,
                     @Value("${bot.token}") String token) {

        // 🔥 ВАЖНО: прокси через системные свойства
        System.setProperty("http.proxyHost", "31.59.20.176");
        System.setProperty("http.proxyPort", "6754");

        System.setProperty("https.proxyHost", "31.59.20.176");
        System.setProperty("https.proxyPort", "6754");

        // 🔐 авторизация
        System.setProperty("http.proxyUser", "jakfihve");
        System.setProperty("http.proxyPassword", "ki9hfpklgrm9");

        System.setProperty("https.proxyUser", "jakfihve");
        System.setProperty("https.proxyPassword", "ki9hfpklgrm9");

        // 🌐 options (без логина/пароля!)
        DefaultBotOptions options = new DefaultBotOptions();
        options.setProxyHost("31.59.20.176");
        options.setProxyPort(6754);
        options.setProxyType(DefaultBotOptions.ProxyType.HTTP);

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