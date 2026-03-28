package com.example.parser.bot.handler;

import com.example.parser.dto.ResultDto;
import com.example.parser.entity.Player;
import com.example.parser.entity.TournamentResultEntity;
import com.example.parser.service.MessageService;
import com.example.parser.service.PlayerService;
import com.example.parser.service.ResultService;
import com.example.parser.service.TournamentResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class TournamentHandler {

    private final ResultService resultService;
    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;
    private final MessageService messageService;

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        ResultService.ParsedResult parsed = resultService.calculateAll(text);
        Long tournamentId = parsed.getTournamentId();
        List<ResultDto> results = parsed.getResults();

        Player player = playerService.getByTelegramId(telegramId);

        String date = results.isEmpty() ? null : results.get(0).getDate();

        StringBuilder sb = new StringBuilder();
        sb.append("🏆 Результаты турнира:\n\n");

        // ✅ дата
        if (date != null) {
            sb.append(formatDate(date)).append("\n\n");
        }

        int i = 1;
        boolean found = false;

        // ✅ вывод результатов
        for (ResultDto r : results) {
            sb.append(i++)
                    .append(". ")
                    .append(r.getPlayer())
                    .append(" — ")
                    .append(r.getTotal())
                    .append("\n");

            if (isSamePlayer(player.getName(), r.getPlayer())) {
                found = true;

                // ✅ сохраняем только если завершён
                if (parsed.isFinished()) {
                    boolean exists = tournamentResultService.exists(
                            player.getId(),
                            tournamentId
                    );

                    if (!exists) {
                        TournamentResultEntity entity = TournamentResultEntity.builder()
                                .player(player)
                                .playerName(r.getPlayer())
                                .amount(r.getTotal())
                                .date(LocalDate.parse(r.getDate()))
                                .tournamentId(tournamentId)
                                .build();

                        tournamentResultService.save(entity);
                    }
                }
            }
        }

        // ✅ финальное сообщение
        if (!parsed.isFinished()) {
            sb.append("\n⏳ Турнир ещё не завершён.\n")
                    .append("Данные показаны на текущий момент и не будут сохранены.\n")
                    .append("Попробуйте снова после завершения турнира — результаты будут зафиксированы автоматически.");
        } else if (found) {
            sb.append("\n✅ Твой результат сохранён!");
        } else {
            sb.append("\n⚠️ Ты не найден в турнире");
        }

        // ✅ отправка
        messageService.send(bot, chatId, sb.toString());

        // 🔥 ВОТ ГЛАВНЫЙ ФИКС
        messageService.sendMenu(bot, chatId, telegramId);
    }

    private String formatDate(String rawDate) {
        try {
            LocalDate date;

            if (rawDate.contains("-")) {
                date = LocalDate.parse(rawDate);
            } else {
                DateTimeFormatter input = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                date = LocalDate.parse(rawDate, input);
            }

            DateTimeFormatter output =
                    DateTimeFormatter.ofPattern("d MMMM yyyy 'года'", new Locale("ru"));

            return "📅 " + date.format(output);

        } catch (Exception e) {
            return rawDate;
        }
    }

    private boolean isSamePlayer(String n1, String n2) {
        List<String> a = List.of(n1.toLowerCase().split(" "));
        List<String> b = List.of(n2.toLowerCase().split(" "));
        return a.containsAll(b) || b.containsAll(a);
    }
}