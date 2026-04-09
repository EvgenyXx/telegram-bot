package com.example.parser.bot.menu;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

public interface MenuExtension {
    void apply(Long telegramId, List<KeyboardRow> rows);
}