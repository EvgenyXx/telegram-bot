package com.example.parser.bot.command;

import com.example.parser.domain.dto.ResultDto;
import com.example.parser.notification.MessageService;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import com.example.parser.tournament.ResultService;
import com.example.parser.tournament.SmartResultService;
import com.example.parser.tournament.TournamentResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
@Order(10)
public class TournamentLinkCommand implements CommandHandler {

    private final PlayerService playerService;
    private final MessageService messageService;
    private final ResultService resultService;
    private final TournamentResultService tournamentResultService;
    private final SmartResultService smartResultService;

    @Override
    public boolean supports(String text, Player player) {
        return text != null && text.startsWith("http");
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {
        String link = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        Player player = playerService.getByTelegramId(telegramId);

        if (player == null) {
            messageService.send(bot, chatId, "❌ Ты не зарегистрирован");
            return;
        }

        // 🔥 парсим
        ResultService.ParsedResult parsed = smartResultService.calculate(link);

        // 🔥 проверка — уже есть?
        boolean alreadyExists = tournamentResultService.exists(
                player,
                parsed.getTournamentId()
        );

        // 🔥 сохраняем / проверяем участие
        boolean found = tournamentResultService.processResults(
                parsed.getResults(),
                player,
                parsed.getTournamentId(),
                parsed.getNightBonus(),
                parsed.isFinished()
        );

        // 🏆 собираем сообщение
        StringBuilder message = new StringBuilder();

        message.append(buildTournamentMessage(parsed));

        message.append("\n────────────\n");

        boolean finished = parsed.isFinished();

        if (alreadyExists) {

            if (!finished) {
                message.append("⏳ Турнир ещё идёт\n");
                message.append("👀 Мы уже отслеживаем его результаты");
            } else {
                message.append("ℹ️ Этот турнир уже был ранее сохранён");
            }

        } else if (!found) {
            message.append("ℹ️ Ты не участвуешь в этом турнире");

        }else {

            if (!finished) {
                message.append("⏳ Турнир ещё идёт\n");
                message.append("📡 Мы начали отслеживание");
            } else {
                message.append("✅ Турнир успешно добавлен в «Мои турниры»");
            }

        }

        messageService.send(bot, chatId, message.toString());
    }

    private String buildTournamentMessage(ResultService.ParsedResult parsed) {
        StringBuilder sb = new StringBuilder();

        sb.append("🏆 Результаты турнира:\n");

        if (parsed.getResults().isEmpty()) {
            return "ℹ️ Нет данных по турниру";
        }

        // дата
        String date = parsed.getResults().get(0).getDate();
        sb.append("📅 ").append(date).append("\n\n");

        // сортировка по убыванию
        List<ResultDto> sorted = parsed.getResults().stream()
                .sorted((a, b) -> Double.compare(b.getTotal(), a.getTotal()))
                .toList();

        int place = 1;
        for (ResultDto r : sorted) {
            sb.append(place++)
                    .append(". ")
                    .append(capitalizeName(r.getPlayer()))
                    .append(" — ")
                    .append(r.getTotal())
                    .append("\n");
        }

        return sb.toString();
    }

    private String capitalizeName(String name) {
        if (name == null || name.isBlank()) return name;

        String[] parts = name.toLowerCase().split(" ");
        StringBuilder result = new StringBuilder();

        for (String p : parts) {
            if (p.isEmpty()) continue;
            result.append(Character.toUpperCase(p.charAt(0)))
                    .append(p.substring(1))
                    .append(" ");
        }

        return result.toString().trim();
    }
}
