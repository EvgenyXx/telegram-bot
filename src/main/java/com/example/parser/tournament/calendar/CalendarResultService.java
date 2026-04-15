package com.example.parser.tournament.calendar;

import com.example.parser.TournamentReportBuilder;
import com.example.parser.notification.MessageService;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import com.example.parser.tournament.TournamentResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;

@Service
@RequiredArgsConstructor
public class CalendarResultService {

    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;
    private final MessageService messageService;
    private final TournamentReportBuilder reportBuilder;

    @Async
    public void processResult(Long chatId,
                              CalendarSession session,
                              TelegramLongPollingBot bot) {

        Player player = resolvePlayer(session);

        if (player == null || session.getState() == null) {
            messageService.send(bot, chatId, "❌ Ошибка состояния");
            return;
        }

        // 📅 Турниры (оставляем текстом)
        if (session.getState() == CalendarState.TOURNAMENTS) {
            var results = tournamentResultService
                    .getResultsByPeriod(player, session.getStart(), session.getEnd());

            StringBuilder sb = new StringBuilder("📅 Турниры:\n\n");

            results.forEach(r ->
                    sb.append(r.getDate())
                            .append(" — ")
                            .append(r.getAmount())
                            .append("\n")
            );

            sb.append("\n📊 Всего турниров: ").append(results.size());

            messageService.send(bot, chatId, sb.toString());
        }

        // 💰 Сумма → теперь через builder (чисто и красиво)
        if (session.getState() == CalendarState.SUM) {

            var stats = tournamentResultService
                    .getStatsByPeriod(player, session.getStart(), session.getEnd());

            if (stats == null) {
                messageService.send(bot, chatId, "❌ Нет данных за период");
                return;
            }

            SendDocument doc = reportBuilder.buildSumDocument(
                    chatId,
                    stats,
                    session.getStart(),
                    session.getEnd()
            );

            try {
                bot.execute(doc);
            } catch (Exception e) {
                messageService.send(bot, chatId, "❌ Ошибка отправки файла");
            }
        }

        // 👇 меню всегда в конце
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