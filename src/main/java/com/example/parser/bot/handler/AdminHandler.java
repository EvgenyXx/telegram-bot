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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // 👉 старт админки
        if (text.equals("📊 Статистика")) {
            List<Player> players = playerService.getAll();

            StringBuilder sb = new StringBuilder("👤 Игроки:\n\n");

            for (Player p : players) {
                sb.append(p.getName()).append("\n");
            }

            sb.append("\nВведи имя и фамилию игрока:");

            userState.put(chatId, "WAITING_PLAYER");

            messageService.send(bot, chatId, sb.toString());
            return;
        }

        // 👉 ввод игрока
        if ("WAITING_PLAYER".equals(userState.get(chatId))) {

            Player player = playerService.findByName(text);

            if (player == null) {
                messageService.send(bot, chatId, "❌ Игрок не найден, попробуй еще раз");
                return;
            }

            selectedPlayer.put(chatId, player);
            userState.put(chatId, "PLAYER_SELECTED");

            messageService.send(bot, chatId,
                    "Выбери действие:\n\n📅 Турниры игрока\n💰 Сумма за период");

            return;
        }

        // 👉 выбор действия
        if ("PLAYER_SELECTED".equals(userState.get(chatId))) {

            if (text.equals("📅 Турниры игрока")) {
                userState.put(chatId, "PLAYER_TOURNAMENTS");
                messageService.send(bot, chatId,
                        "Введи период:\nнапример:\n01.03.2026 01.04.2026");
                return;
            }

            if (text.equals("💰 Сумма за период")) {
                userState.put(chatId, "PLAYER_SUM");
                messageService.send(bot, chatId,
                        "Введи период:\nнапример:\n01.03.2026 01.04.2026");
                return;
            }
        }

        // 👉 ввод периода
        if (isDateRange(text)) {

            Player player = selectedPlayer.get(chatId);

            String state = userState.get(chatId);

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
                            sb.append(r.getDate()).append(" — ").append(r.getAmount()).append("\n")
                    );
                }

                messageService.send(bot, chatId, sb.toString());
            }

            if ("PLAYER_SUM".equals(state)) {

                PeriodStatsProjection stats =
                        tournamentResultService.getStatsByPeriod(player, start, end);

                String response = "💰 Сумма: " + stats.getSum() +
                        "\n📊 Среднее: " + stats.getAverage() +
                        "\n💸 Сумма -3%: " + stats.getMinusThreePercent();

                messageService.send(bot, chatId, response);
            }

            // сброс
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

}