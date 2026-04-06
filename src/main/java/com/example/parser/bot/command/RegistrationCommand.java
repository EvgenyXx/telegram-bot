package com.example.parser.bot.command;

import com.example.parser.config.AdminProperties;
import com.example.parser.notification.MessageService;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class RegistrationCommand implements CommandHandler {

    private final PlayerService playerService;
    private final MessageService messageService;
    private final AdminProperties adminProperties;

    @Override
    public boolean supports(String text, Player player) {
        return player == null || player.getName() == null;
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) {

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        log.info("📝 REGISTER INPUT: {}", text);

        if (!isValidFullName(text)) {
            messageService.send(bot, chatId,
                    "❌ Введи: Фамилия Имя\nПример: Иванов Иван");
            return;
        }

        playerService.registerIfNotExists(telegramId, text);

        for (Long adminId : adminProperties.getAdmins()) {
            messageService.send(bot, adminId,
                    "🆕 Новый пользователь:\n👤 " + text +
                            "\n🆔 " + telegramId);
        }

        messageService.send(bot, chatId,
                "✅ Вы зарегистрированы: " + text);

        messageService.sendMenu(bot, chatId, telegramId, null);
    }

    private boolean isValidFullName(String text) {
        return text.matches("^[А-Яа-яЁё]+\\s[А-Яа-яЁё]+$");
    }
}