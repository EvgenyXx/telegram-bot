package com.example.parser.bot.command;


import com.example.parser.bot.handler.AdminHandler;

import com.example.parser.modules.notification.service.MessageService;
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
public class SearchCommand implements CommandHandler {

    private final AdminHandler adminHandler;
    private final AdminProperties adminProperties;
    private final MessageService messageService;

    @Override
    public boolean supports(String text, Player player) {
        return text.startsWith("/search")
                && player != null
                && adminProperties.isAdmin(player.getTelegramId());
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        // /search Иван
        String[] parts = text.split(" ", 2);

        if (parts.length < 2) {
            messageService.send(bot, chatId, "❗ Используй: /search Иван");
            return;
        }

        String query = parts[1];

        adminHandler.search(chatId, query, bot);
    }
}