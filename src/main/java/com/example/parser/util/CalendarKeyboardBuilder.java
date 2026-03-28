package com.example.parser.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class CalendarKeyboardBuilder {

    public static InlineKeyboardMarkup build(YearMonth month) {

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // 👉 заголовок (месяц)
        rows.add(List.of(button(month.getMonth().name() + " " + month.getYear(), "ignore")));

        // 👉 дни недели
        rows.add(List.of(
                button("Пн","ignore"),
                button("Вт","ignore"),
                button("Ср","ignore"),
                button("Чт","ignore"),
                button("Пт","ignore"),
                button("Сб","ignore"),
                button("Вс","ignore")
        ));

        LocalDate firstDay = month.atDay(1);
        int shift = firstDay.getDayOfWeek().getValue(); // 1-7

        List<InlineKeyboardButton> week = new ArrayList<>();

        // 👉 пустые ячейки
        for (int i = 1; i < shift; i++) {
            week.add(button(" ", "ignore"));
        }

        int length = month.lengthOfMonth();

        for (int day = 1; day <= length; day++) {

            LocalDate date = month.atDay(day);

            week.add(button(
                    String.valueOf(day),
                    "date_" + date
            ));

            if (week.size() == 7) {
                rows.add(week);
                week = new ArrayList<>();
            }
        }

        if (!week.isEmpty()) {
            rows.add(week);
        }

        // 👉 переключение месяцев
        rows.add(List.of(
                button("⬅️", "month_" + month.minusMonths(1)),
                button("➡️", "month_" + month.plusMonths(1))
        ));

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);

        return keyboard;
    }

    private static InlineKeyboardButton button(String text, String data) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(text);
        btn.setCallbackData(data);
        return btn;
    }
}