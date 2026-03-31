package com.example.parser.bot.keyboard;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CalendarKeyboardBuilder {

    public static InlineKeyboardMarkup build(
            YearMonth month,
            LocalDate start,
            LocalDate end
    ) {

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        String monthName = month.getMonth().getDisplayName(
                TextStyle.FULL,
                new Locale("ru")
        );

        rows.add(List.of(button(monthName + " " + month.getYear(), "ignore")));

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
        int shift = firstDay.getDayOfWeek().getValue();

        List<InlineKeyboardButton> week = new ArrayList<>();

        for (int i = 1; i < shift; i++) {
            week.add(button(" ", "ignore"));
        }

        int length = month.lengthOfMonth();

        for (int day = 1; day <= length; day++) {

            LocalDate date = month.atDay(day);
            String text = String.valueOf(day);

// 👉 есть только старт
            if (start != null && end == null) {
                if (date.equals(start)) {
                    text = "🟢" + day;
                }
            }

// 👉 есть и старт и конец
            if (start != null && end != null) {

                // точки A и B
                if (date.equals(start)) {
                    text = "🟢" + day; // A
                } else if (date.equals(end)) {
                    text = "🔴" + day; // B
                }

                // диапазон между
                else if (date.isAfter(start) && date.isBefore(end)) {
                    text = "🟩" + day;
                }//todo вынести логику покраски в другой класс
            }

            week.add(button(text, "date_" + date));

            if (week.size() == 7) {
                rows.add(week);
                week = new ArrayList<>();
            }
        }

        if (!week.isEmpty()) {
            rows.add(week);
        }

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