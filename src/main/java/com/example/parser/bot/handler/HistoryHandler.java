package com.example.parser.bot.handler;

import com.example.parser.dto.PeriodStatsProjection;
import com.example.parser.entity.Player;
import com.example.parser.entity.TournamentResultEntity;
import com.example.parser.service.MessageService;
import com.example.parser.service.PlayerService;
import com.example.parser.service.TournamentResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import org.telegram.telegrambots.meta.api.objects.Update;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HistoryHandler {

    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;
    private final MessageService messageService;

    public void handle(Update update, TelegramLongPollingBot bot) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        if (text.equals("📅 Мои турниры")) {
            messageService.send(bot, chatId,
                    "Введи период:\nнапример:\n01.03.2026 01.04.2026");
            return;
        }

        String[] parts = text.split(" ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        LocalDate start = LocalDate.parse(parts[0], formatter);
        LocalDate end = LocalDate.parse(parts[1], formatter);

        Player player = playerService.getByTelegramId(chatId);

        List<TournamentResultEntity> results =
                tournamentResultService.getResultsByPeriod(player, start, end);

        StringBuilder sb = new StringBuilder("📅 Твои турниры:\n\n");

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

        messageService.send(bot, chatId, sb.toString());
        messageService.sendMenu(bot, chatId, chatId);
    }

    public void handleSum(Update update, TelegramLongPollingBot bot) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        if (text.equals("💰 Сумма за период")) {
            messageService.send(bot, chatId,
                    "Введи период:\nнапример:\n01.03.2026 01.04.2026");
            return;
        }

        String[] parts = text.split(" ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        LocalDate start = LocalDate.parse(parts[0], formatter);
        LocalDate end = LocalDate.parse(parts[1], formatter);

        Player player = playerService.getByTelegramId(chatId);

        PeriodStatsProjection stats =
                tournamentResultService.getStatsByPeriod(player, start, end);

        String response =
                "💰 Сумма: " + stats.getSum() + "\n" +
                        "📊 Среднее: " + stats.getAverage() + "\n" +
                        "💸 После -3%: " + stats.getMinusThreePercent();

        messageService.send(bot, chatId, response);
        messageService.sendMenu(bot, chatId, chatId);
    }
}