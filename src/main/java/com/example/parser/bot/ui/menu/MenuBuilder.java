package com.example.parser.bot.ui.menu;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MenuBuilder {

    private final List<MenuExtension>extensions;

    public ReplyKeyboardMarkup buildMainMenu(Long telegramId) {

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();

        // базовые кнопки
        rows.add(createRow("📅 Мои турниры", "💰 Сумма за период"));
        rows.add(createRow("📊 Моя статистика"));

        // расширения (админские и любые будущие)
        for (MenuExtension extension : extensions) {
            extension.apply(telegramId, rows);
        }

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private KeyboardRow createRow(String... buttons) {
        KeyboardRow row = new KeyboardRow();
        for (String text : buttons) {
            row.add(new org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton(text));
        }
        return row;
    }
}

