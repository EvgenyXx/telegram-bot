package com.example.parser.modules.lineup.api;

import com.example.parser.modules.lineup.domain.Lineup;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Component
public class LineupMessageBuilder {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    public void sendLineups(TelegramLongPollingBot bot,
                            Long chatId,
                            List<Lineup> lineups) {
        try {
            sendFile(bot, chatId, lineups);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка отправки составов", e);
        }
    }

    private void sendFile(TelegramLongPollingBot bot,
                          Long chatId,
                          List<Lineup> lineups) throws Exception {

        if (lineups == null || lineups.isEmpty()) {
            return;
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

        InputFile file = new InputFile(
                new ByteArrayInputStream(
                        sb.toString().getBytes(StandardCharsets.UTF_8)
                ),
                "составы.txt"
        );

        SendDocument doc = new SendDocument();
        doc.setChatId(chatId.toString());
        doc.setDocument(file);

        bot.execute(doc);
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