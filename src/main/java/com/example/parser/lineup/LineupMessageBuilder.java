package com.example.parser.lineup;

import com.example.parser.domain.entity.Lineup;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class LineupMessageBuilder {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    public InputFile buildTomorrowFile(List<Lineup> lineups) {
        if (lineups == null || lineups.isEmpty()) {
            return null;
        }

        String text = buildTomorrowMessage(lineups);
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        return new InputFile(
                new ByteArrayInputStream(bytes),
                buildFileName()
        );
    }

    public String buildTomorrowMessage(List<Lineup> lineups) {
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

        // 🔥 сортировка строго по времени (с 00:00 вверх)
        List<Lineup> sorted = lineups.stream()
                .sorted(Comparator.comparing(Lineup::getTime))
                .toList();

        for (Lineup l : sorted) {

            sb.append("⏰ ")
                    .append(l.getTime())
                    .append("\n");

            // 👉 добавляем лигу
            sb.append("🏆 Лига ")
                    .append(l.getLeague())
                    .append("\n");

            // 👉 игроки
            Stream.of(l.getPlayers().split(","))
                    .map(String::trim)
                    .map(this::shortName)
                    .forEach(player ->
                            sb.append("👤 ")
                                    .append(player)
                                    .append("\n")
                    );

            sb.append("\n"); // отступ между матчами
        }

        return sb.toString();
    }

    private String buildFileName() {
        LocalDate tomorrow = LocalDate.now(ZONE).plusDays(1);
        return "составы_" + tomorrow.format(DATE_FORMAT) + ".txt";
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