package com.example.parser.bot.command;


import com.example.parser.config.StreamProperties;
import com.example.parser.notification.MessageService;
import com.example.parser.player.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class StreamCommand implements CommandHandler{


    private final StreamProperties streamProperties;
    private final MessageService messageService;

    private final String STREAM = "/stream";

    @Override
    public boolean supports(String text, Player player) {
        return STREAM.equals(text);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) {
        Long chatId = update.getMessage().getChatId();

        if (streamProperties.getHalls() == null || streamProperties.getHalls().isEmpty()) {
            messageService.send(bot, chatId, "Нет доступных трансляций 😢");
            return;
        }
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (StreamProperties.Hall hall : streamProperties.getHalls()) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("🎥 " + hall.getName());
            button.setUrl(hall.getUrl());

            rows.add(List.of(button));
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);

        messageService.sendInlineKeyboard(
                bot,
                chatId,
                "🎥 Выбери трансляцию:",
                keyboard
        );
    }
}
