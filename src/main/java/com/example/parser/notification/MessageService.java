package com.example.parser.notification;

import com.example.parser.bot.MenuBuilder;
import com.example.parser.bot.ParserBot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ParserBot bot;
    private final MenuBuilder menuBuilder;

    private final Map<Long, Integer> menuMessages = new ConcurrentHashMap<>();
    private final Map<Long, Integer> inlineMessages = new ConcurrentHashMap<>();

    // ================== SEND ==================

    public void send(Long chatId, String text) {
        SendMessage msg = createMessage(chatId, text);
        try {
            bot.execute(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Message sendInlineKeyboardAndGetMessage(Long chatId,
                                                   String text,
                                                   InlineKeyboardMarkup keyboard) {
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

    public void sendInlineKeyboard(Long chatId,
                                   String text,
                                   InlineKeyboardMarkup keyboard) {
        sendInlineKeyboardAndGetMessage(chatId, text, keyboard);
    }

    public void sendMenu(Long chatId, Long telegramId, String context) {
        try {
            SendMessage message = createMessage(chatId, buildMenuText(context));
            message.setReplyMarkup(menuBuilder.buildMainMenu(telegramId));

            Message sent = bot.execute(message);
            menuMessages.put(chatId, sent.getMessageId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Message sendAndReturn(Long chatId, String text) {
        try {
            SendMessage message = createMessage(chatId, text);
            return bot.execute(message);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ================== EDIT ==================

    public void editMessage(Long chatId,
                            Integer messageId,
                            String text,
                            InlineKeyboardMarkup keyboard) {
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

    public void delete(Long chatId, Integer messageId) {
        try {
            DeleteMessage delete = new DeleteMessage();
            delete.setChatId(chatId.toString());
            delete.setMessageId(messageId);

            bot.execute(delete);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}