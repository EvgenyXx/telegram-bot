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

        return buildTournamentMessage(parsed) +
                "\n────────────\n" +
                buildStatusMessage(result, parsed);
    }

    private String buildStatusMessage(TournamentLinkResult result,
                                      ResultService.ParsedResult parsed) {

        boolean alreadyExists = result.isAlreadyExists();
        boolean found = result.isFound();
        boolean finished = parsed.isFinished();

        if (alreadyExists) {
            return buildAlreadyExistsMessage(finished);
        }

        if (!found) {
            return "ℹ️ Ты не участвуешь в этом турнире";
        }

        return buildNewTournamentMessage(finished);
    }

    private String buildAlreadyExistsMessage(boolean finished) {
        if (!finished) {
            return "⏳ Турнир ещё идёт\n👀 Мы уже отслеживаем его результаты";
        }
        return "ℹ️ Этот турнир уже был ранее сохранён";
    }

    private String buildNewTournamentMessage(boolean finished) {
        if (!finished) {
            return "⏳ Турнир ещё идёт\n📡 Мы начали отслеживание";
        }
        return "✅ Турнир успешно добавлен в «Мои турниры»";
    }

    private String buildTournamentMessage(ResultService.ParsedResult parsed) {
        if (parsed.getResults().isEmpty()) {
            return "ℹ️ Нет данных по турниру";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🏆 Результаты турнира:\n");

        String date = parsed.getResults().get(0).getDate();
        sb.append("📅 ").append(date).append("\n\n");

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