package com.example.parser.tournament.calendar;

import com.example.parser.TournamentListReportBuilder;
import com.example.parser.TournamentReportBuilder;
import com.example.parser.domain.dto.TournamentResult;
import com.example.parser.notification.MessageService;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import com.example.parser.tournament.TournamentResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor//todo исправить эту помойку
public class CalendarResultService {

    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;
    private final MessageService messageService;
    private final TournamentReportBuilder reportBuilder;
    private final TournamentListReportBuilder listReportBuilder;

    @Async
    public void processResult(Long chatId,
                              CalendarSession session,
                              TelegramLongPollingBot bot) {

        Player player = resolvePlayer(session);

        if (player == null || session.getState() == null) {
            messageService.send(bot, chatId, "❌ Ошибка состояния");
            return;
        }

        // 📅 ТУРНИРЫ → ЧЕРЕЗ BUILDER
        if (session.getState() == CalendarState.TOURNAMENTS) {

            var entities = tournamentResultService
                    .getResultsByPeriod(player, session.getStart(), session.getEnd());

            if (entities == null || entities.isEmpty()) {
                messageService.send(bot, chatId, "❌ Нет турниров за период");
                return;
            }

            // 🔥 маппим Entity → DTO (как надо билдеру)
            var results = List.of(
                    new TournamentResult(
                            null, // 👈 лига тебе сейчас не нужна
                            entities.stream()
                                    .collect(Collectors.toMap(
                                            e -> e.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                            e -> e.getAmount() == null ? 0 : e.getAmount().intValue(),
                                            Integer::sum
                                    ))
                    )
            );

            // 🚀 вот главный вызов
            listReportBuilder.sendTournamentReport(
                    bot,
                    chatId,
                    results,
                    session.getStart(),
                    session.getEnd()
            );
        }

        // 💰 СУММА (без изменений)
        if (session.getState() == CalendarState.SUM) {

            var stats = tournamentResultService
                    .getStatsByPeriod(player, session.getStart(), session.getEnd());

            if (stats == null) {
                messageService.send(bot, chatId, "❌ Нет данных за период");
                return;
            }

            String text = reportBuilder.buildSumMessage(
                    stats,
                    session.getStart(),
                    session.getEnd()
            );

            messageService.send(bot, chatId, text);
        }

        // 👇 меню
        messageService.sendMenu(bot, chatId, session.getTelegramId(), null);
    }

    private Player resolvePlayer(CalendarSession session) {
        if (session.getPlayerId() != null) {
            return playerService.findById(session.getPlayerId());
        }
        if (session.getTelegramId() != null) {
            return playerService.getByTelegramId(session.getTelegramId());
        }
        return null;
    }
}