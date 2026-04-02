package com.example.parser.bot;

import com.example.parser.config.AdminProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MenuBuilder {

    private final AdminProperties adminProperties;

    public ReplyKeyboardMarkup buildMainMenu(Long telegramId) {

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        List<KeyboardRow> rows = new ArrayList<>();

        rows.add(createRow("📅 Мои турниры", "💰 Сумма за период"));
        rows.add(createRow("📊 Моя статистика"));
        rows.add(createRow("🔥 Лайв матч"));

        if (adminProperties.isAdmin(telegramId)) {
            rows.add(createRow("📊 Статистика"));
        }

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private KeyboardRow createRow(String... buttons) {
        KeyboardRow row = new KeyboardRow();
        for (String btn : buttons) {
            row.add(btn);
        }
        return row;
    }
}