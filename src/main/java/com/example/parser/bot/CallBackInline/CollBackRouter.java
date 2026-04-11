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



        for (CallBackAction action : actions) {
            if (action.support(data)) {
                action.handle(update, bot);
                return; // 💥 важно!
            }
        }

        // 🔥 ЕСЛИ НИЧЕГО НЕ НАШЛОСЬ
        log.warn("NO HANDLER: data={}", data);
    }
}