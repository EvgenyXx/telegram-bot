package com.example.parser.bot.menu;


import com.example.parser.modules.shared.AdminProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SuperAdminMenuExtension implements MenuExtension {

    private final AdminProperties adminProperties;

    @Override
    public void apply(Long telegramId, List<KeyboardRow> rows) {
        if (!adminProperties.isSuperAdmin(telegramId)) return;

        KeyboardRow row = new KeyboardRow();
        row.add("🔥 Лайв матч");
        rows.add(row);
    }
}