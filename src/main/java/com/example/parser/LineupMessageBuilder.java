package com.example.parser;

import com.example.parser.domain.entity.Lineup;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class LineupMessageBuilder {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public String buildTomorrowMessage(List<Lineup> lineups) {

        if (lineups.isEmpty()) {
            return "❌ Нет составов на завтра";
        }

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String dateStr = tomorrow.format(DATE_FORMAT);

        StringBuilder sb = new StringBuilder();

        sb.append("📋 Ростов — составы на ")
                .append(dateStr)
                .append("\n\n");

        sb.append("```\n");

        lineups.stream()
                .sorted(Comparator.comparing(Lineup::getLeague)
                        .thenComparing(Lineup::getTime))
                .forEach(l -> {

                    String players = l.getPlayers()
                            .replaceAll("\\s+", " ")
                            .trim();

                    // 🔥 ВЫРАВНИВАНИЕ КОЛОНОК
                    sb.append(String.format(
                            "%-3s %-6s %s\n",
                            l.getLeague(),
                            l.getTime(),
                            players
                    ));
                });

        sb.append("```");

        return sb.toString();
    }
}