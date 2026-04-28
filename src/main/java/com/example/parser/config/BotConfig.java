package com.example.parser.config;

import com.example.parser.bot.transport.ParserBot;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@RequiredArgsConstructor
public class BotConfig {

    private final ParserBot parserBot;

    @Value("${proxy.host:}")
    private String proxyHost;

    @Value("${proxy.port:1080}")
    private int proxyPort;

    @Value("${proxy.secret:}")
    private String proxySecret;

    @PostConstruct
    public void init() throws Exception {
        DefaultBotOptions botOptions = new DefaultBotOptions();

        if (proxyHost != null && !proxyHost.isEmpty() &&
                proxySecret != null && !proxySecret.isEmpty()) {
            botOptions.setProxyHost(proxyHost);
            botOptions.setProxyPort(proxyPort);
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
            System.out.println("Proxy ON: " + proxyHost + ":" + proxyPort);
        }

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(parserBot);
        } catch (TelegramApiException e) {
            System.err.println("Failed to register bot: " + e.getMessage());
        }
    }
}