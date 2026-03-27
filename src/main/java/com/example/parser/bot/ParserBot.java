package com.example.parser.bot;

import com.example.parser.service.ResultService;
import com.example.parser.dto.ResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ParserBot extends TelegramLongPollingBot {

    private final ResultService resultService;

    @Value("${bot.token}")
    private String token;



    @Override
    public String getBotUsername() {
        return "@table_tennis_parser_bot";
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {

            if (update.hasMessage() && update.getMessage().hasText()) {

                String text = update.getMessage().getText();
                Long chatId = update.getMessage().getChatId();

                // 👉 КНОПКИ
                if (text.equals("➕ Добавить турнир")) {
                    execute(sendText(chatId, "Скинь ссылку на турнир 👇"));
                    return;
                }

                if (text.equals("💰 Посчитать")) {
                    execute(sendText(chatId, "Введи период:\nнапример:\n01.03.2026 01.04.2026"));
                    return;
                }

                // 👉 ССЫЛКА
                if (text.startsWith("http")) {
                    List<ResultDto> results = resultService.calculateAll(text);

                    String date = results.isEmpty() ? null : results.get(0).getDate();

                    StringBuilder sb = new StringBuilder();
                    sb.append("🏆 Результаты турнира:\n\n");
                    sb.append(formatDate(date)).append("\n\n");

                    int i = 1;
                    for (ResultDto r : results) {
                        sb.append(i++).append(". ")
                                .append(r.getPlayer())
                                .append(" — ")
                                .append(r.getTotal())
                                .append("\n");
                    }

                    execute(sendText(chatId, sb.toString()));
                    execute(sendMenu(chatId)); // 👈 вернуть меню
                    return;
                }

                // 👉 ВСЁ ОСТАЛЬНОЕ → меню
                execute(sendMenu(chatId));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SendMessage sendText(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        return message;
    }

    private String formatDate(String rawDate) {
        try {
            java.time.LocalDate date;

            // если формат 2026-03-27
            if (rawDate.contains("-")) {
                date = java.time.LocalDate.parse(rawDate);
            }
            // если формат 27.03.2026
            else if (rawDate.contains(".")) {
                java.time.format.DateTimeFormatter input =
                        java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");
                date = java.time.LocalDate.parse(rawDate, input);
            }
            else {
                return rawDate;
            }

            java.time.format.DateTimeFormatter output =
                    java.time.format.DateTimeFormatter.ofPattern(
                            "d MMMM yyyy 'года'",
                            new java.util.Locale("ru")
                    );

            return "📅 " + date.format(output);

        } catch (Exception e) {
            return rawDate; // fallback
        }
    }

    private SendMessage sendMenu(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Выбери действие 👇");

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("➕ Добавить турнир");
        row1.add("💰 Посчитать");

        keyboard.setKeyboard(List.of(row1));

        message.setReplyMarkup(keyboard);

        return message;
    }
}