package com.example.parser.bot.command;

import com.example.parser.config.AdminProperties;
import com.example.parser.notification.MessageService;
import com.example.parser.player.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Order(3)
public class AdminCommand implements CommandHandler {

    private final MessageService messageService;
    private final AdminProperties adminProperties;

    @Override
    public boolean supports(String text, Player player) {
        return "📊 Статистика".equals(text)
                && player != null
                && adminProperties.isAdmin(player.getTelegramId());
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) {
        Long chatId = update.getMessage().getChatId();

        messageService.send(bot, chatId, "🔍 Введи имя или фамилию игрока");
    }
}