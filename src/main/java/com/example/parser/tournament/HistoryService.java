package com.example.parser.tournament;

import com.example.parser.notification.MessageService;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;
    private final MessageService messageService;

    public void handleHistory(Update update, TelegramLongPollingBot bot) {

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        if (text.equals("📅 Мои турниры")) {
            messageService.send(bot, chatId,
                    "Введи период:\nнапример:\n01.03.2026 01.04.2026");
            return;
        }

        LocalDate[] dates = parseDates(text);

        Player player = playerService.getByTelegramId(telegramId);

        var results = tournamentResultService.getResultsByPeriod(
                player,
                dates[0],
                dates[1]
        );

        StringBuilder sb = new StringBuilder("📅 Твои турниры:\n\n");

        if (results.isEmpty()) {
            sb.append("❌ Ничего не найдено");
        } else {
            results.forEach(r ->
                    sb.append(r.getDate())
                            .append(" — ")
                            .append(r.getAmount())
                            .append("\n"));
        }

        messageService.send(bot, chatId, sb.toString());
        messageService.sendMenu(bot, chatId, telegramId, null);
    }

    public void handleSum(Update update, TelegramLongPollingBot bot) {

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        if (text.equals("💰 Сумма за период")) {
            messageService.send(bot, chatId,
                    "Введи период:\nнапример:\n01.03.2026 01.04.2026");
            return;
        }

        LocalDate[] dates = parseDates(text);

        Player player = playerService.getByTelegramId(telegramId);

        var stats = tournamentResultService.getStatsByPeriod(
                player,
                dates[0],
                dates[1]
        );

        String response =
                "💰 Сумма: " + stats.getSum() +
                        "\n📊 Среднее: " + stats.getAverage() +
                        "\n💸 После -3%: " + stats.getMinusThreePercent();

        messageService.send(bot, chatId, response);
        messageService.sendMenu(bot, chatId, telegramId, null);
    }

    // 🔥 ОБЩИЙ МЕТОД
    private LocalDate[] parseDates(String text) {
        String[] parts = text.split(" ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        LocalDate start = LocalDate.parse(parts[0], formatter);
        LocalDate end = LocalDate.parse(parts[1], formatter);

        return new LocalDate[]{start, end};
    }
}