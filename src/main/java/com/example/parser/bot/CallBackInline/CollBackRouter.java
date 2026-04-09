package com.example.parser.bot.CallBackInline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class CollBackRouter {

    private final List<CallBackAction> actions;

    public void route(Update update, TelegramLongPollingBot bot) throws Exception {

        String data = update.getCallbackQuery().getData();

        log.debug("Routing callback: data={}", data);

        for (CallBackAction action : actions) {
            if (action.support(data)) {

                log.debug("Handler found: {} for data={}",
                        action.getClass().getSimpleName(), data);

                action.handle(update, bot);

                return; // 💥 важно!
            }
        }

        log.warn("No handler found for callback data={}", data);
    }
}