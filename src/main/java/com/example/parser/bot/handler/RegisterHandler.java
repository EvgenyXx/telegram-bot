package com.example.parser.bot.handler;

import com.example.parser.domain.entity.Player;
import com.example.parser.service.MessageService;
import com.example.parser.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class RegisterHandler {

    private final PlayerService playerService;
    private final MessageService messageService;

    public void handle(Update update, TelegramLongPollingBot bot) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        // ✅ ЕСЛИ УЖЕ ЕСТЬ — НЕ РЕГИСТРИРУЕМ
        Player existing = playerService.getByTelegramId(telegramId);
        if (existing != null) {
            messageService.send(bot, chatId, "Ты уже зарегистрирован: " + existing.getName());
            messageService.sendMenu(bot, chatId, telegramId);
            return;
        }

        // ❌ проверка имени
        if (!isValidFullName(text)) {
            messageService.send(bot, chatId,
                    "❌ Введи имя и фамилию правильно\nпример: Иван Иванов");
            return;
        }

        // ✅ регистрация
        playerService.registerIfNotExists(telegramId, text);

        messageService.send(bot, chatId, "✅ Вы зарегистрированы: " + text);
        messageService.sendMenu(bot, chatId, telegramId);
    }

    private boolean isValidFullName(String text) {
        return text.matches("^[А-Яа-яЁё]+\\s[А-Яа-яЁё]+$");
    }
}