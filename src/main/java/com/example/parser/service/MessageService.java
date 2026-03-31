package com.example.parser.service;

import com.example.parser.MenuBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class MessageService {

    private final MenuBuilder menuBuilder;

    private final Map<Long, Integer> menuMessages = new ConcurrentHashMap<>();
    private final Map<Long, Integer> inlineMessages = new ConcurrentHashMap<>();

    // ================== SEND ==================

    public void send(TelegramLongPollingBot bot, Long chatId, String text) {
        try {
            bot.execute(createMessage(chatId, text));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 👉 основной метод (с сохранением messageId)
    public Message sendInlineKeyboardAndGetMessage(
            TelegramLongPollingBot bot,
            Long chatId,
            String text,
            InlineKeyboardMarkup keyboard
    ) throws Exception {

        SendMessage message = createMessage(chatId, text);
        message.setReplyMarkup(keyboard);

        Message sent = bot.execute(message);

        inlineMessages.put(chatId, sent.getMessageId());

        return sent;
    }

    // 👉 чтобы старый код не ломался
    public void sendInlineKeyboard(
            TelegramLongPollingBot bot,
            Long chatId,
            String text,
            InlineKeyboardMarkup keyboard
    ) throws Exception {

        sendInlineKeyboardAndGetMessage(bot, chatId, text, keyboard);
    }

    public void sendMenu(TelegramLongPollingBot bot, Long chatId, Long telegramId) {
        sendMenu(bot, chatId, telegramId, null);
    }

    public void sendMenu(TelegramLongPollingBot bot, Long chatId, Long telegramId, String context) {
        try {
            SendMessage message = createMessage(chatId, buildMenuText(context));
            message.setReplyMarkup(menuBuilder.buildMainMenu(telegramId));

            Message sent = bot.execute(message);
            menuMessages.put(chatId, sent.getMessageId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(TelegramLongPollingBot bot, Long chatId, Integer messageId) {
        try {
            DeleteMessage delete = new DeleteMessage();
            delete.setChatId(chatId.toString());
            delete.setMessageId(messageId);
            bot.execute(delete);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================== DELETE ==================
//
//    public void deleteMenu(TelegramLongPollingBot bot, Long chatId) {
//        Integer messageId = menuMessages.get(chatId);
//        if (messageId == null) return;
//
//        try {
//            bot.execute(new DeleteMessage(chatId.toString(), messageId));
//        } catch (Exception ignored) {}
//
//        menuMessages.remove(chatId);
//    }

//    public void deleteInline(TelegramLongPollingBot bot, Long chatId) {
//        Integer messageId = inlineMessages.get(chatId);
//        if (messageId == null) return;
//
//        try {
//            bot.execute(new DeleteMessage(chatId.toString(), messageId));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        inlineMessages.remove(chatId);
//    }

//    public void clearUI(TelegramLongPollingBot bot, Long chatId) {
//        deleteInline(bot, chatId);
//        deleteMenu(bot, chatId);
//    }

    // ================== EDIT ==================

    public void editMessage(
            TelegramLongPollingBot bot,
            Long chatId,
            Integer messageId,
            String text,
            InlineKeyboardMarkup keyboard
    ) throws Exception {

        EditMessageText edit = new EditMessageText();
        edit.setChatId(chatId.toString());
        edit.setMessageId(messageId);
        edit.setText(text);
        edit.setParseMode("Markdown");
        edit.setReplyMarkup(keyboard);

        bot.execute(edit);
    }

    public Integer getInlineMessageId(Long chatId) {
        return inlineMessages.get(chatId);
    }

    // ================== PRIVATE ==================

    private SendMessage createMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("Markdown");
        return message;
    }

    private String buildMenuText(String context) {
        if (context != null) {
            return "👤 " + context + "\n\nВыбери действие 👇";
        }
        return "Выбери действие 👇";
    }

//    public void sendWithKeyboard(TelegramLongPollingBot bot,
//                                 Long chatId,
//                                 String text,
//                                 InlineKeyboardMarkup keyboard) {
//
//        try {
//            SendMessage message = new SendMessage();
//            message.setChatId(chatId.toString());
//            message.setText(text);
//            message.setReplyMarkup(keyboard);
//
//            bot.execute(message);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}