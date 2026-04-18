package com.example.parser.bot.callback;

import com.example.parser.bot.handler.AdminHandler;
import com.example.parser.modules.notification.service.MessageService;
import com.example.parser.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnblockUserCallback implements ActionCallback {

    private final AdminHandler adminHandler;
    private final PlayerService playerService;
    private final MessageService messageService;

    private static final String UNBLOCK_USER = "unblock_user_";

    @Override
    public boolean support(String data) {
        return data.startsWith(UNBLOCK_USER);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        Long playerId = Long.parseLong(data.replace(UNBLOCK_USER, ""));

        log.debug("Unblock user callback: chatId={}, playerId={}", chatId, playerId);

        playerService.unblock(playerId);

        messageService.send(bot, chatId, "✅ Пользователь разблокирован");

        adminHandler.handlePlayerSelected(chatId, playerId, bot);

        log.debug("User unblocked successfully: chatId={}, playerId={}", chatId, playerId);
    }
}