package com.example.parser.bot;

import com.example.parser.dto.ResultDto;
import com.example.parser.entity.Player;
import com.example.parser.entity.TournamentResultEntity;
import com.example.parser.service.PlayerService;
import com.example.parser.service.ResultService;
import com.example.parser.service.TournamentResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ParserBot extends TelegramLongPollingBot {

    private final ResultService resultService;
    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;

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
            if (!(update.hasMessage() && update.getMessage().hasText())) return;

            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // 👉 START
            if (text.equals("/start")) {
                execute(sendText(chatId, "Введите имя и фамилию 👇"));
                return;
            }

            // 👉 КНОПКА: мои турниры
            if (text.equals("📅 Мои турниры")) {
                execute(sendText(chatId,
                        "Введи период:\nнапример:\n01.03.2026 01.04.2026"));
                return;
            }

            // 👉 ПЕРИОД (даты)
            if (text.matches("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}\\.\\d{2}\\.\\d{4}")) {

                String[] parts = text.split(" ");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

                LocalDate start = LocalDate.parse(parts[0], formatter);
                LocalDate end = LocalDate.parse(parts[1], formatter);

                Player player = playerService.getByTelegramId(chatId);

                List<TournamentResultEntity> results =
                        tournamentResultService.getResultsByPeriod(player, start, end);

                StringBuilder sb = new StringBuilder();
                sb.append("📅 Твои турниры:\n\n");

                if (results.isEmpty()) {
                    sb.append("❌ Ничего не найдено");
                } else {
                    for (TournamentResultEntity r : results) {
                        sb.append(r.getDate())
                                .append(" — ")
                                .append(r.getAmount())
                                .append("\n");
                    }
                }

                execute(sendText(chatId, sb.toString()));
                execute(sendMenu(chatId));
                return;
            }

            // 👉 ССЫЛКА (парсинг турнира)
            if (text.startsWith("http")) {

                List<ResultDto> results = resultService.calculateAll(text);
                Player player = playerService.getByTelegramId(chatId);

                String date = results.isEmpty() ? null : results.get(0).getDate();

                StringBuilder sb = new StringBuilder();
                sb.append("🏆 Результаты турнира:\n\n");
                sb.append(formatDate(date)).append("\n\n");

                int i = 1;
                boolean found = false;

                for (ResultDto r : results) {
                    sb.append(i++)
                            .append(". ")
                            .append(r.getPlayer())
                            .append(" — ")
                            .append(r.getTotal())
                            .append("\n");

                    if (isSamePlayer(player.getName(), r.getPlayer())) {

                        TournamentResultEntity entity = TournamentResultEntity.builder()
                                .player(player)
                                .playerName(r.getPlayer())
                                .amount(r.getTotal())
                                .date(LocalDate.parse(r.getDate()))
                                .build();

                        tournamentResultService.save(entity);
                        found = true;
                    }
                }

                if (found) {
                    sb.append("\n✅ Твой результат сохранён!");
                } else {
                    sb.append("\n⚠️ Ты не найден в турнире");
                }

                execute(sendText(chatId, sb.toString()));
                execute(sendMenu(chatId));
                return;
            }

            // 👉 РЕГИСТРАЦИЯ (ТОЛЬКО если не команда и не ссылка)
            if (!text.startsWith("http")
                    && !text.equals("📅 Мои турниры")
                    && !text.equals("/start")) {

                playerService.registerIfNotExists(chatId, text);

                execute(sendText(chatId, "✅ Вы зарегистрированы: " + text));
                execute(sendMenu(chatId));
                return;
            }

            // 👉 fallback
            execute(sendMenu(chatId));

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
            LocalDate date;

            if (rawDate.contains("-")) {
                date = LocalDate.parse(rawDate);
            } else if (rawDate.contains(".")) {
                DateTimeFormatter input = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                date = LocalDate.parse(rawDate, input);
            } else {
                return rawDate;
            }

            DateTimeFormatter output = DateTimeFormatter.ofPattern(
                    "d MMMM yyyy 'года'", new Locale("ru"));

            return "📅 " + date.format(output);

        } catch (Exception e) {
            return rawDate;
        }
    }

    private SendMessage sendMenu(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Выбери действие 👇");

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("📅 Мои турниры");

        keyboard.setKeyboard(List.of(row1));
        message.setReplyMarkup(keyboard);

        return message;
    }

    private String normalize(String name) {
        return name.trim().toLowerCase();
    }

    private boolean isSamePlayer(String name1, String name2) {
        List<String> n1 = List.of(normalize(name1).split(" "));
        List<String> n2 = List.of(normalize(name2).split(" "));
        return n1.containsAll(n2) || n2.containsAll(n1);
    }
}