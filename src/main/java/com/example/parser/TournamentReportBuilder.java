package com.example.parser;

import com.example.parser.domain.dto.PeriodStatsProjection;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class TournamentReportBuilder {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public SendDocument buildSumDocument(Long chatId,
                                         PeriodStatsProjection stats,
                                         LocalDate start,
                                         LocalDate end) {

        StringBuilder sb = new StringBuilder();

        sb.append("📅 Период: ")
                .append(start.format(DATE_FORMAT))
                .append(" - ")
                .append(end.format(DATE_FORMAT))
                .append("\n\n");

        sb.append("💰 Сумма: ").append(stats.getSum()).append("\n");
        sb.append("📊 Среднее: ").append(stats.getAverage()).append("\n");
        sb.append("💸 Сумма -3%: ").append(stats.getMinusThreePercent()).append("\n");
        sb.append("🎯 Турниров: ").append(stats.getCount()).append("\n");

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        InputFile file = new InputFile(
                new ByteArrayInputStream(bytes),
                buildFileName(start, end)
        );

        SendDocument doc = new SendDocument();
        doc.setChatId(chatId.toString());
        doc.setDocument(file);

        return doc;
    }

    private String buildFileName(LocalDate start, LocalDate end) {
        return "сумма_за_период_" +
                start.format(DATE_FORMAT) +
                "_" +
                end.format(DATE_FORMAT) +
                ".txt";
    }
}