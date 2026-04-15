package com.example.parser;

import com.example.parser.domain.dto.PeriodStatsProjection;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class TournamentReportBuilder {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    // 🔥 ТЕКСТ (основной вариант)
    public String buildSumMessage(PeriodStatsProjection stats,
                                  LocalDate start,
                                  LocalDate end) {

        return "💰 Сумма за период\n\n" +
                "📅 " + start.format(DATE_FORMAT) +
                " - " + end.format(DATE_FORMAT) + "\n\n" +
                "💰 Сумма: " + formatMoney(stats.getSum()) + "\n" +
                "📊 Среднее: " + formatMoney(stats.getAverage()) + "\n" +
                "💸 Сумма -3%: " + formatMoney(stats.getMinusThreePercent()) + "\n" +
                "🎯 Турниров: " + stats.getCount();
    }

    // 💰 формат денег
    private String formatMoney(Double value) {
        if (value == null) return "0 ₽";
        return String.format("%,.0f ₽", value);
    }
}