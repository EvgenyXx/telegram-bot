package com.example.parser.bot.callback;

import com.example.parser.bot.handler.AdminHandler;
import com.example.parser.modules.tournament.calendar.service.CalendarSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlayerSelectCallback implements ActionCallback {

    private final CalendarSessionService sessionService;
    private final AdminHandler adminHandler;

    private static final String PLAYER = "player_";

    @Override
    public boolean support(String data) {
        return data.startsWith(PLAYER);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        log.debug("Player select callback: chatId={}, data={}", chatId, data);

        try {
            Long playerId = Long.parseLong(data.replace(PLAYER, ""));

            sessionService.remove(chatId);

            adminHandler.handlePlayerSelected(chatId, playerId, bot);

            log.debug("Player selected: chatId={}, playerId={}", chatId, playerId);

        } catch (NumberFormatException e) {
            log.error("Invalid playerId in callback: chatId={}, data={}", chatId, data, e);
        }
    }
}