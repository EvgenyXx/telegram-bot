package com.example.parser.modules.notification.formatter;

import com.example.parser.core.dto.TournamentResult;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class TournamentListReportBuilder {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public void sendTournamentReport(TelegramLongPollingBot bot,
                                     Long chatId,
                                     List<TournamentResult> results,
                                     LocalDate start,
                                     LocalDate end) {
        try {
            sendFile(bot, chatId, results, start, end);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка отправки турниров", e);
        }
    }

    private void sendFile(TelegramLongPollingBot bot,
                          Long chatId,
                          List<TournamentResult> results,
                          LocalDate start,
                          LocalDate end) throws Exception {

        StringBuilder fileContent = new StringBuilder();

        fileContent.append("📅 Твои турниры\n\n");
        fileContent.append(start.format(DATE_FORMAT))
                .append(" - ")
                .append(end.format(DATE_FORMAT))
                .append("\n\n");

        int count = 0;

        for (TournamentResult tr : results) {
            for (var entry : tr.getResults().entrySet()) {
                fileContent.append(entry.getKey())
                        .append(" — ")
                        .append(formatMoney(entry.getValue()))
                        .append("\n");
                count++;
            }
        }

        fileContent.append("\n📊 Кол-во: ").append(count);

        InputFile file = new InputFile(
                new ByteArrayInputStream(
                        fileContent.toString().getBytes(StandardCharsets.UTF_8)
                ),
                "tournaments_" +
                        start.format(DATE_FORMAT) + "_" +
                        end.format(DATE_FORMAT) + ".txt"
        );

        SendDocument doc = new SendDocument();
        doc.setChatId(chatId.toString());
        doc.setDocument(file);

        bot.execute(doc);
    }

    private String formatMoney(Integer value) {
        if (value == null) return "0 ₽";
        return String.format("%,d ₽", value);
    }
}