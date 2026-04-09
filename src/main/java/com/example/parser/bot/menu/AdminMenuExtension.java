package com.example.parser.bot.menu;

import com.example.parser.config.AdminProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminMenuExtension implements MenuExtension {

    private final AdminProperties adminProperties;

    @Override
    public void apply(Long telegramId, List<KeyboardRow> rows) {
        if (!adminProperties.isAdmin(telegramId)) return;

        KeyboardRow row = new KeyboardRow();
        row.add("📊 Статистика");
        rows.add(row);
    }
}