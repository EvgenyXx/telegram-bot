package com.example.parser;

import java.util.regex.Pattern;

public class NamePartValidator {

    private static final Pattern PATTERN = Pattern.compile("^[А-ЯЁ][а-яё]+$");

    public static void validate(String input, String fieldName) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("❌ " + fieldName + " не может быть пустым");
        }

        if (!PATTERN.matcher(input.trim()).matches()) {
            throw new IllegalArgumentException(
                    "❌ Введите " + fieldName + " корректно.\n\n" +
                            "Только кириллица, без цифр и символов.\n" +
                            "С большой буквы.\n\n" +
                            "Пример: Иванов / Антон"
            );
        }
    }
}