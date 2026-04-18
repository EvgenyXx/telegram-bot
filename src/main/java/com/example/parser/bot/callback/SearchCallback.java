package com.example.parser.bot.callback;

import com.example.parser.bot.handler.AdminHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j//todo если что откатить
public class SearchCallback implements ActionCallback {

    private static final String SEARCH = "search|";

    private final AdminHandler adminHandler;

    @Override
    public boolean support(String data) {
        return data.startsWith(SEARCH);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        log.debug("Search callback: chatId={}, data={}", chatId, data);

        try {
            String[] parts = data.split("\\|");

            if (parts.length < 3) {
                log.warn("Invalid search callback format: chatId={}, data={}", chatId, data);
                return;
            }

            String query = parts[1];
            int page = Integer.parseInt(parts[2]);

            adminHandler.searchPage(chatId, query, page, bot);

            log.debug("Search executed: chatId={}, query={}, page={}", chatId, query, page);

        } catch (Exception e) {
            log.error("Error handling search callback: chatId={}, data={}", chatId, data, e);
        }
    }
}