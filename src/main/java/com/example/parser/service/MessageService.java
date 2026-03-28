package com.example.parser.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessageService {

    private static final Long ADMIN_ID = 459307336L;

    public void send(TelegramLongPollingBot bot, Long chatId, String text) {
        try {
            bot.execute(new SendMessage(chatId.toString(), text));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMenu(TelegramLongPollingBot bot, Long chatId) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("Выбери действие 👇");

            ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
            keyboard.setResizeKeyboard(true);

            // 👉 первая строка (основные действия)
            KeyboardRow row1 = new KeyboardRow();
            row1.add("📅 Мои турниры");
            row1.add("💰 Сумма за период");

            List<KeyboardRow> rows = new ArrayList<>();
            rows.add(row1);

            // 👉 отдельная строка только для админа
            if (chatId.equals(ADMIN_ID)) {
                KeyboardRow adminRow = new KeyboardRow();
                adminRow.add("📊 Статистика");
                rows.add(adminRow);
            }

            keyboard.setKeyboard(rows);
            message.setReplyMarkup(keyboard);

            bot.execute(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendWithKeyboard(TelegramLongPollingBot bot,
                                 Long chatId,
                                 String text,
                                 ReplyKeyboardMarkup keyboard) {

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboard);

        try {
            bot.execute(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}