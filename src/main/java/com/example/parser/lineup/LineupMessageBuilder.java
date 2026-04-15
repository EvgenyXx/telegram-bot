package com.example.parser.lineup;

import com.example.parser.domain.entity.Lineup;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class LineupMessageBuilder {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    public String buildTomorrowMessage(List<Lineup> lineups) {
        if (lineups == null || lineups.isEmpty()) {
            return "❌ Нет составов";
        }

        LocalDate tomorrow = LocalDate.now(ZONE).plusDays(1);
        String updateTime = LocalTime.now(ZONE).format(TIME_FORMAT);

        StringBuilder sb = new StringBuilder();

        sb.append("📋 Составы — ")
                .append(tomorrow.format(DATE_FORMAT))
                .append("\n");

        sb.append("🕒 Обновлено: ")
                .append(updateTime)
                .append("\n\n");

        List<Lineup> sorted = lineups.stream()
                .sorted(Comparator.comparing(Lineup::getTime))
                .toList();

        for (Lineup l : sorted) {

            sb.append("⏰ ")
                    .append(l.getTime())
                    .append(" | Лига ")
                    .append(l.getLeague())
                    .append("\n");

            sb.append("──────────────\n");

            String[] players = l.getPlayers().split(",");

            for (String playerRaw : players) {
                sb.append("• ")
                        .append(shortName(playerRaw.trim()))
                        .append("\n");
            }

            sb.append("\n");
        }

        return sb.toString();
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