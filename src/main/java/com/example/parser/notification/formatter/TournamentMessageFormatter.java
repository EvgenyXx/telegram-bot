package com.example.parser.notification.formatter;

import com.example.parser.domain.dto.ResultDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TournamentMessageFormatter {

    public String formatResults(List<ResultDto> results, double bonus) {
        StringBuilder sb = new StringBuilder();

        int i = 1;

        for (ResultDto r : results) {
            double finalAmount = r.getTotal() + bonus;

            sb.append(i++)
                    .append(". ")
                    .append(r.getPlayer())
                    .append(" — ");

            // 🔥 разная логика
            if (bonus > 0) {
                sb.append((int) r.getTotal())
                        .append(" + 🌙")
                        .append((int) bonus)
                        .append(" = ")
                        .append((int) finalAmount);
            } else {
                sb.append((int) r.getTotal());
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public String formatFinalWithPlayer(List<ResultDto> results,
                                        double bonus,
                                        String playerName) {

        StringBuilder sb = new StringBuilder();

        sb.append("🏆 Результаты турнира:\n");

        if (!results.isEmpty() && results.get(0).getDate() != null) {
            sb.append("📅 ")
                    .append(formatDate(results.get(0).getDate()))
                    .append("\n\n");
        }

        int i = 1;
        for (ResultDto r : results) {

            double finalAmount = r.getTotal() + bonus;

            sb.append(i).append(". ")
                    .append(r.getPlayer().toLowerCase())
                    .append(" — ")
                    .append((int) finalAmount)
                    .append("\n");

            i++;
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
                    java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy 'года'", new java.util.Locale("ru"));

            return date.format(output);

        } catch (Exception e) {
            return rawDate;
        }
    }
}