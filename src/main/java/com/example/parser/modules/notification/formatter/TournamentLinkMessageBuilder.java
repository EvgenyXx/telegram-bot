package com.example.parser.modules.notification.formatter;

import com.example.parser.core.dto.ResultDto;
import com.example.parser.core.dto.TournamentLinkResult;
import com.example.parser.modules.tournament.domain.TournamentLinkStatus;
import com.example.parser.modules.tournament.service.ResultService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TournamentLinkMessageBuilder {

    public String build(TournamentLinkResult result) {

        TournamentLinkStatus status = result.getStatus();

        if (status == TournamentLinkStatus.NOT_PARTICIPATING) {
            return """
            ℹ️ Ты не участвуешь в этом турнире
            
            Мы не будем добавлять его в твои турниры.
            """;
        }

        ResultService.ParsedResult parsed = result.getParsed();

        if (parsed == null) {
            return buildStatusMessage(status);
        }

        return buildTournamentMessage(parsed) +
                "\n────────────\n" +
                buildStatusMessage(status);
    }

    private String buildStatusMessage(TournamentLinkStatus status) {
        return switch (status) {
            case USER_ALREADY_EXISTS -> "ℹ️ Турнир уже сохранён у тебя";
            case TRACKING_STARTED -> "📡 Турнир добавлен, мы начали отслеживание";
            case ALREADY_TRACKED -> "📡 Мы уже отслеживаем этот турнир";
            case FINISHED -> "🏁 Турнир завершён и сохранён";
            default -> "❌ Неизвестный статус";
        };
    }

    private String buildTournamentMessage(ResultService.ParsedResult parsed) {

        if (parsed == null || parsed.getResults().isEmpty()) {
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