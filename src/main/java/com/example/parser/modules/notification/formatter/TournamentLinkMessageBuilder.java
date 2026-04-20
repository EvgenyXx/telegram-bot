package com.example.parser.modules.notification.formatter;

import com.example.parser.core.dto.ResultDto;
import com.example.parser.core.dto.TournamentLinkResult;
import com.example.parser.modules.tournament.domain.TournamentLinkStatus;
import com.example.parser.modules.tournament.service.result.ParsedResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TournamentLinkMessageBuilder {

    public String build(TournamentLinkResult result) {

        TournamentLinkStatus status = result.getStatus();
        ParsedResult  parsed = result.getParsed();

        // ❌ не участвует
        if (status == TournamentLinkStatus.NOT_PARTICIPATING) {
            return """
            ℹ️ Ты не найден в результатах турнира
            
            Мы не будем добавлять его в твои турниры.
            """;
        }

        // ⏳ турнир не начался
        if (status == TournamentLinkStatus.NOT_STARTED) {
            return """
            ⏳ Турнир ещё не начался
            
            Результаты пока недоступны.
            Мы начнём отслеживание, как только появятся данные.
            """;
        }

        // если вообще нет parsed
        if (parsed == null) {
            return buildStatusMessage(status);
        }

        return buildTournamentMessage(parsed) +
                "\n────────────\n" +
                buildStatusMessage(status);
    }

    private String buildStatusMessage(TournamentLinkStatus status) {
        return switch (status) {

            case TRACKING_STARTED ->
                    """
                    📡 Турнир добавлен
                    Начали отслеживание результатов
                    """;

            case ALREADY_TRACKED ->
                    """
                    📡 Турнир уже отслеживается
                    Результаты обновляются автоматически
                    """;

            case FINISHED ->
                    """
                    🏁 Турнир завершён
                    Итоговые результаты сохранены
                    """;

            default ->
                    """
                    ❌ Ошибка
                    Не удалось определить статус турнира
                    """;
        };
    }

    private String buildTournamentMessage(ParsedResult parsed) {

        if (parsed == null || parsed.getResults() == null || parsed.getResults().isEmpty()) {
            return "ℹ️ Пока нет данных по турниру";
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