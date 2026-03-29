package com.example.parser.service;

import com.example.parser.config.AdminProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MessageService {

//    private static final List<Long> ADMINS = List.of(
//            459307336L, 1632772141L, 5429880868L
//    );

    private final AdminProperties adminProperties;
    private final Map<Long, Integer> menuMessages = new HashMap<>();

    public void send(TelegramLongPollingBot bot, Long chatId, String text) {
        try {
            bot.execute(new SendMessage(chatId.toString(), text));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMenu(TelegramLongPollingBot bot, Long chatId, Long telegramId) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("Выбери действие 👇");

            ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
            keyboard.setResizeKeyboard(true);

            KeyboardRow row1 = new KeyboardRow();
            row1.add("📅 Мои турниры");
            row1.add("💰 Сумма за период");

            KeyboardRow row2 = new KeyboardRow();
            row2.add("📊 Моя статистика");

            List<KeyboardRow> rows = new ArrayList<>();
            rows.add(row1);
            rows.add(row2);

            if (adminProperties.isAdmin(telegramId)) {
                KeyboardRow adminRow = new KeyboardRow();
                adminRow.add("📊 Статистика");
                rows.add(adminRow);
            }

            keyboard.setKeyboard(rows);
            message.setReplyMarkup(keyboard);

            Message sent = bot.execute(message);

            // сохраняем messageId
            menuMessages.put(chatId, sent.getMessageId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteMenu(TelegramLongPollingBot bot, Long chatId) {
        Integer messageId = menuMessages.get(chatId);

        if (messageId == null) return;

        try {
            bot.execute(new org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage(
                    chatId.toString(),
                    messageId
            ));
        } catch (Exception ignored) {}

        menuMessages.remove(chatId);
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

    public Message sendInlineKeyboardAndGetMessage(
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