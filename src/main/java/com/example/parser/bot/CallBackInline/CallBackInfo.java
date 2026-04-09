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
public class CallBackInfo implements CallBackAction {

    private final LiveMatchHandler liveMatchHandler;

    private static final String INFO = "info";

    @Override
    public boolean support(String data) {
        return INFO.equals(data);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        log.debug("Info callback: chatId={}", chatId);

        liveMatchHandler.sendInfo(chatId, bot);

        log.debug("Info sent: chatId={}", chatId);
    }
}