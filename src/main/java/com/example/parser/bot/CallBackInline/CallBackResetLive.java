package com.example.parser.bot.CallBackInline;

import com.example.parser.bot.handler.LiveMatchHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallBackResetLive implements CallBackAction {

    private final LiveMatchHandler liveMatchHandler;

    private static final String RESET_LIVE = "reset_live";

    @Override
    public boolean support(String data) {
        return RESET_LIVE.equals(data);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();

        log.debug("Reset live callback: chatId={}, data={}", chatId, data);

        liveMatchHandler.stop(chatId, bot);

        log.debug("Live match stopped: chatId={}", chatId);
    }
}