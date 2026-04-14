package com.example.parser.lineup;

import com.example.parser.domain.entity.Lineup;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LineupMessageBuilder {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    public String buildTomorrowMessage(List<Lineup> lineups) {

        if (lineups.isEmpty()) {
            return "❌ Нет составов на завтра";
        }

        LocalDate tomorrow = LocalDate.now(ZONE).plusDays(1);
        String dateStr = tomorrow.format(DATE_FORMAT);
        String updateTime = LocalTime.now(ZONE).format(TIME_FORMAT);

        StringBuilder sb = new StringBuilder();

        sb.append("📋 Ростов — ")
                .append(dateStr)
                .append("\n");

        sb.append("🕒 Обновлено: ")
                .append(updateTime)
                .append("\n\n");

        String currentLeague = "";

        List<Lineup> sorted = lineups.stream()
                .sorted(Comparator.comparing(Lineup::getLeague)
                        .thenComparing(Lineup::getTime))
                .toList();

        for (Lineup l : sorted) {

            String players = formatPlayers(l.getPlayers());

            if (!l.getLeague().equals(currentLeague)) {
                currentLeague = l.getLeague();
                sb.append("🏆 Лига ")
                        .append(currentLeague)
                        .append("\n");
            }

            sb.append("⏰ ")
                    .append(l.getTime())
                    .append(" — ")
                    .append(players)
                    .append("\n");
        }

        return sb.toString();
    }

    private String formatPlayers(String players) {
        return List.of(players.split(","))
                .stream()
                .map(String::trim)
                .map(this::shortName)
                .collect(Collectors.joining(", "));
    }

    private String shortName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0];
        }

        String lastName = parts[0];
        String firstInitial = parts[1].substring(0, 1);

        return lastName + " " + firstInitial + ".";
    }
}