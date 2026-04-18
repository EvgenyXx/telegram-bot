package com.example.parser.bot.command;

import com.example.parser.bot.handler.CommandHandler;
import com.example.parser.bot.handler.LiveMatchHandler;

import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.shared.AdminProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Order(4)
public class LiveMatchCommand implements CommandHandler {

    private final LiveMatchHandler liveMatchHandler;
    private final AdminProperties adminProperties;

    @Override
    public boolean supports(String text, Player player) {
        return text.equals("🔥 Лайв матч")
                && player != null
                && adminProperties.isSuperAdmin(player.getTelegramId());
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {
        Long chatId = update.getMessage().getChatId();
        liveMatchHandler.start(chatId, bot);
    }
}