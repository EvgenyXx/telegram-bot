package com.example.parser.notification;

import com.example.parser.bot.MenuBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MenuBuilder menuBuilder;

    private final Map<Long, Integer> menuMessages = new ConcurrentHashMap<>();
    private final Map<Long, Integer> inlineMessages = new ConcurrentHashMap<>();//todo проверить

    // ================== SEND ==================

    public void send(TelegramLongPollingBot bot, Long chatId, String text) {
        try {
            bot.execute(createMessage(chatId, text));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Message sendInlineKeyboardAndGetMessage(
            TelegramLongPollingBot bot,
            Long chatId,
            String text,
            InlineKeyboardMarkup keyboard
    ) {
        try {
            SendMessage message = createMessage(chatId, text);
            message.setReplyMarkup(keyboard);

            Message sent = bot.execute(message);
            inlineMessages.put(chatId, sent.getMessageId());

            return sent;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendInlineKeyboard(TelegramLongPollingBot bot,
                                   Long chatId,
                                   String text,
                                   String buttonText,
                                   String callbackData) {

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(buttonText);
        button.setCallbackData(callbackData);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(button)));

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(markup);

        try {
            bot.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendInlineKeyboard(
            TelegramLongPollingBot bot,
            Long chatId,
            String text,
            InlineKeyboardMarkup keyboard
    ) {
        sendInlineKeyboardAndGetMessage(bot, chatId, text, keyboard);
    }

    public void sendMenu(
            TelegramLongPollingBot bot,
            Long chatId,
            Long telegramId,
            String context
    ) {
        try {
            SendMessage message = createMessage(chatId, buildMenuText(context));
            message.setReplyMarkup(menuBuilder.buildMainMenu(telegramId));

            Message sent = bot.execute(message);
            menuMessages.put(chatId, sent.getMessageId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Message sendAndReturn(TelegramLongPollingBot bot, Long chatId, String text) {
        try {
            SendMessage message = createMessage(chatId, text);
            return bot.execute(message);
        } catch (Exception e) {
            e.printStackTrace();//todo проверить
            return null;
        }
    }

    // ================== EDIT ==================

    public void editMessage(
            TelegramLongPollingBot bot,
            Long chatId,
            Integer messageId,
            String text,
            InlineKeyboardMarkup keyboard
    ) {
        try {
            EditMessageText edit = new EditMessageText();
            edit.setChatId(chatId.toString());
            edit.setMessageId(messageId);
            edit.setText(text);
            edit.setParseMode("Markdown");
            edit.setReplyMarkup(keyboard);

            bot.execute(edit);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}