package com.example.parser.bot.handler;

import com.example.parser.service.MessageService;
import com.example.parser.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;


import java.util.List;

@Component
@RequiredArgsConstructor
public class RegisterHandler {

    private final PlayerService playerService;
    private final MessageService messageService;

    public void handle(Update update, TelegramLongPollingBot bot) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        if (!isValidFullName(text)) {
            messageService.send(bot, chatId,
                    "❌ Не будь Александром Хабовцом 😄\n" +
                            "Введи имя и фамилию правильно\n" +
                            "пример: Иван Иванов");
            return;
        }

        playerService.registerIfNotExists(chatId, text);

        messageService.send(bot, chatId, "✅ Вы зарегистрированы: " + text);
        messageService.sendMenu(bot, chatId);
    }

    private boolean isValidFullName(String text) {
        return text.matches("^[А-Яа-яЁё]+\\s[А-Яа-яЁё]+$");
    }
}