package com.example.parser.bot.callback;

import com.example.parser.bot.handler.LiveMatchHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
public class LiveMatchInfoCallback implements ActionCallback {

    private final LiveMatchHandler liveMatchHandler;

    private static final String LIVE_MATCH = "live_match";

    @Override
    public boolean support(String data) {
        return LIVE_MATCH.equals(data);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        log.debug("Live match callback: chatId={}, data={}", chatId, data);

        liveMatchHandler.start(chatId, bot);

        log.debug("Live match started: chatId={}", chatId);
    }
}