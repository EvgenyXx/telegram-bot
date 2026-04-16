package com.example.parser.modules.notification.formatter;

import com.example.parser.core.dto.ResultDto;
import com.example.parser.core.dto.TournamentLinkResult;
import com.example.parser.modules.tournament.service.ResultService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TournamentLinkMessageBuilder {

    public String build(TournamentLinkResult result) {

        ResultService.ParsedResult parsed = result.getParsed();
        boolean alreadyExists = result.isAlreadyExists();
        boolean found = result.isFound();

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
        } else {
            if (!finished) {
                message.append("⏳ Турнир ещё идёт\n");
                message.append("📡 Мы начали отслеживание");
            } else {
                message.append("✅ Турнир успешно добавлен в «Мои турниры»");
            }
        }

        return message.toString();
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