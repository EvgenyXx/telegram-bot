package com.example.parser.bot.handler;

import com.example.parser.dto.PeriodStatsProjection;
import com.example.parser.entity.Player;
import com.example.parser.service.MessageService;
import com.example.parser.service.PlayerService;
import com.example.parser.service.TournamentResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public class AdminHandler {

    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;
    private final MessageService messageService;

    private final Map<Long, String> userState = new HashMap<>();
    private final Map<Long, Player> selectedPlayer = new HashMap<>();

    public void handle(Update update, TelegramLongPollingBot bot) {

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        // 👉 СТАРТ
        if (text.equals("📊 Статистика")) {

            List<Player> players = playerService.getAll();

            InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rows = new ArrayList<>();

            for (Player p : players) {
                InlineKeyboardButton btn = new InlineKeyboardButton();
                btn.setText(p.getName());
                btn.setCallbackData("player_" + p.getId());
                rows.add(List.of(btn));
            }

            keyboard.setKeyboard(rows);

            messageService.sendInlineKeyboard(
                    bot,
                    chatId,
                    "Выбери игрока 👇",
                    keyboard
            );

            return;
        }

        // 👉 ВВОД ПЕРИОДА
        if (isDateRange(text)) {

            Player player = selectedPlayer.get(chatId);
            String state = userState.get(chatId);

            if (player == null || state == null) {
                messageService.send(bot, chatId, "❌ Ошибка состояния, начни заново");
                return;
            }

            String[] parts = text.split(" ");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            LocalDate start = LocalDate.parse(parts[0], formatter);
            LocalDate end = LocalDate.parse(parts[1], formatter);

            if ("PLAYER_TOURNAMENTS".equals(state)) {

                var results = tournamentResultService.getResultsByPeriod(player, start, end);

                StringBuilder sb = new StringBuilder("📅 Турниры игрока:\n\n");

                if (results.isEmpty()) {
                    sb.append("❌ Ничего не найдено");
                } else {
                    results.forEach(r ->
                            sb.append(r.getDate())
                                    .append(" — ")
                                    .append(r.getAmount())
                                    .append("\n")
                    );
                }

                messageService.send(bot, chatId, sb.toString());
            }

            if ("PLAYER_SUM".equals(state)) {

                PeriodStatsProjection stats =
                        tournamentResultService.getStatsByPeriod(player, start, end);

                String response =
                        "💰 Сумма: " + stats.getSum() +
                                "\n📊 Среднее: " + stats.getAverage() +
                                "\n💸 Сумма -3%: " + stats.getMinusThreePercent();

                messageService.send(bot, chatId, response);
            }

            userState.remove(chatId);
            selectedPlayer.remove(chatId);

            messageService.sendMenu(bot, chatId);
        }
    }

    private boolean isDateRange(String text) {
        return text.matches("\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}\\.\\d{2}\\.\\d{4}");
    }

    public boolean isInProgress(Long chatId) {
        return userState.containsKey(chatId);
    }

    // 👉 КЛИК ПО ИГРОКУ
    public void handlePlayerSelected(Long chatId, Long playerId, TelegramLongPollingBot bot) {

        Player player = playerService.findById(playerId);

        selectedPlayer.put(chatId, player);
        userState.put(chatId, "PLAYER_SELECTED");

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

        InlineKeyboardButton btn1 = new InlineKeyboardButton();
        btn1.setText("📅 Турниры");
        btn1.setCallbackData("tournaments");

        InlineKeyboardButton btn2 = new InlineKeyboardButton();
        btn2.setText("💰 Сумма");
        btn2.setCallbackData("sum");

        keyboard.setKeyboard(List.of(List.of(btn1, btn2)));

        messageService.sendInlineKeyboard(
                bot,
                chatId,
                "Выбери действие 👇",
                keyboard
        );
    }

    // 👉 СПРОСИТЬ ПЕРИОД
    public void askPeriod(Long chatId, TelegramLongPollingBot bot, String state) {

        userState.put(chatId, state);

        messageService.send(
                bot,
                chatId,
                "Введи период:\nнапример:\n01.03.2026 01.04.2026"
        );
    }
}