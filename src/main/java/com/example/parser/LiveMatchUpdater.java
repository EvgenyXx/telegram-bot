package com.example.parser;

import com.example.parser.bot.handler.LiveMatchHandler;
import com.example.parser.service.LiveMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class LiveMatchUpdater {

    private final LiveMatchHandler liveMatchHandler;
    private final LiveMatchService liveMatchService;

    @Async
    public void start(Long chatId, TelegramLongPollingBot bot) {

        while (liveMatchService.isAutoUpdating(chatId)) {
            try {
                TimeUnit.SECONDS.sleep(5);
                liveMatchHandler.handleLiveMatch(chatId, bot);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}