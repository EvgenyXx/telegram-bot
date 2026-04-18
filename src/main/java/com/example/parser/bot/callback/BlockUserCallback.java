package com.example.parser.bot.callback;


import com.example.parser.bot.handler.AdminHandler;

import com.example.parser.modules.notification.service.MessageService;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.service.PlayerService;
import com.example.parser.modules.shared.AdminProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
@Component
@Slf4j
public class BlockUserCallback implements ActionCallback {

    private final PlayerService playerService;
    private final AdminProperties adminProperties;
    private final MessageService messageService;
    private final AdminHandler adminHandler;

    private static final String BLOCK_USER = "block_user_";

    @Override
    public boolean support(String data) {
        return data.startsWith(BLOCK_USER);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        String data = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long playerId = Long.parseLong(data.replace(BLOCK_USER, ""));

        log.debug("Block user callback: chatId=" + chatId + ", playerId=" + playerId);

        Player target = playerService.findById(playerId);

        if (target != null && adminProperties.isAdmin(target.getTelegramId())) {
            log.warn("Attempt to block admin: playerId=" + playerId);
            messageService.send(bot, chatId, "❌ Нельзя заблокировать администратора");
            return;
        }

        playerService.block(playerId);

        log.info("User blocked: playerId=" + playerId);

        messageService.send(bot, chatId, "🚫 Пользователь заблокирован");
        adminHandler.handlePlayerSelected(chatId, playerId, bot);
    }
}
