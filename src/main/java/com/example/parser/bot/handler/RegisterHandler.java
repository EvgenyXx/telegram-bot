package com.example.parser.bot.handler;

import com.example.parser.service.MessageService;
import com.example.parser.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RegisterHandler {

    private final PlayerService playerService;
    private final MessageService messageService;

    public void handle(Update update, TelegramLongPollingBot bot) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        playerService.registerIfNotExists(chatId, text);

        messageService.send(bot, chatId, "✅ Вы зарегистрированы: " + text);
        messageService.sendMenu(bot, chatId);
    }
}