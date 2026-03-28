package com.example.parser.service;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessageService {

    private static final List<Long> ADMINS = List.of(
            459307336L,
            1632772141L,
            5429880868L
    );

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

            KeyboardRow row1 = new KeyboardRow();
            row1.add("📅 Мои турниры");
            row1.add("💰 Сумма за период");

            List<KeyboardRow> rows = new ArrayList<>();
            rows.add(row1);

            if (chatId.equals(ADMINS)) {
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

    public void sendInlineKeyboard(TelegramLongPollingBot bot,
                                   Long chatId,
                                   String text,
                                   InlineKeyboardMarkup keyboard) {

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

    // 🔥 ВАЖНО — используем для редактируемого календаря
    public org.telegram.telegrambots.meta.api.objects.Message sendInlineKeyboardAndGetMessage(
            TelegramLongPollingBot bot,
            Long chatId,
            String text,
            InlineKeyboardMarkup keyboard
    ) throws Exception {

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboard);

        return bot.execute(message);
    }
}