package com.example.parser.bot.keyboard;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;


@Component
public class TournamentResultEditKeyboardBuilder {

    private static final String TEXT_KEYBOARD = "✏️ Изменить результат";
    private static final String CALLBACK_DATA = "adjust_sum_";

    public InlineKeyboardMarkup build(Long playerId, Long tournamentId) {
        InlineKeyboardButton edit = new InlineKeyboardButton();
        edit.setText(TEXT_KEYBOARD);
        edit.setCallbackData(
                CALLBACK_DATA + playerId + "_" + tournamentId
        );
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(edit)));
        return markup;
    }

}
