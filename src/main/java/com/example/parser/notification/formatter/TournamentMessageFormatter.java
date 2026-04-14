package com.example.parser.notification.formatter;

import com.example.parser.domain.dto.ResultDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TournamentMessageFormatter {

    public String format(List<ResultDto> results) {
        StringBuilder sb = new StringBuilder();

        sb.append("🏁 Турнир завершён, результаты посчитаны\n");
        sb.append("🏆 Результаты турнира:\n");

        if (!results.isEmpty() && results.get(0).getDate() != null) {
            sb.append("📅 ").append(formatDate(results.get(0).getDate())).append("\n\n");
        }

        int limit = Math.min(results.size(), 10);

        for (int i = 0; i < limit; i++) {
            ResultDto r = results.get(i);

            sb.append(i + 1).append(". ")
                    .append(r.getPlayer())
                    .append(" — ")
                    .append(r.getTotal())
                    .append("\n");
        }

        return sb.toString();
    }

    private String formatDate(String rawDate) {
        try {
            java.time.LocalDate date;

            if (rawDate.contains("-")) {
                date = java.time.LocalDate.parse(rawDate);
            } else {
                java.time.format.DateTimeFormatter input =
                        java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");
                date = java.time.LocalDate.parse(rawDate, input);
            }

            java.time.format.DateTimeFormatter output =
                    java.time.format.DateTimeFormatter.ofPattern(
                            "d MMMM yyyy 'года'",
                            new java.util.Locale("ru")
                    );

            return date.format(output);

        } catch (Exception e) {
            return rawDate;
        }
    }
}