package com.example.parser;

import com.example.parser.domain.dto.PeriodStatsProjection;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Отчет");

            // 🔥 Стили
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle valueStyle = createValueStyle(workbook);

            int rowIdx = 0;

            // 📅 Период
            Row periodRow = sheet.createRow(rowIdx++);
            Cell periodCell = periodRow.createCell(0);
            periodCell.setCellValue("Период: " +
                    start.format(DATE_FORMAT) + " - " +
                    end.format(DATE_FORMAT));

            rowIdx++; // пустая строка

            // 🔥 Заголовки таблицы
            Row headerRow = sheet.createRow(rowIdx++);
            createCell(headerRow, 0, "📊 Показатель", headerStyle);
            createCell(headerRow, 1, "💰 Значение", headerStyle);

            // 📊 Данные
            createRow(sheet, rowIdx++, "Сумма", formatMoney(stats.getSum()), valueStyle);
            createRow(sheet, rowIdx++, "Среднее", formatMoney(stats.getAverage()), valueStyle);
            createRow(sheet, rowIdx++, "Сумма -3%", formatMoney(stats.getMinusThreePercent()), valueStyle);
            createRow(sheet, rowIdx++, "Кол-во турниров", String.valueOf(stats.getCount()), valueStyle);

            // 📏 автоширина
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            workbook.write(out);

            InputFile file = new InputFile(
                    new ByteArrayInputStream(out.toByteArray()),
                    buildFileName(start, end)
            );

            SendDocument doc = new SendDocument();
            doc.setChatId(chatId.toString());
            doc.setDocument(file);

            return doc;

        } catch (Exception e) {
            throw new RuntimeException("Ошибка создания Excel файла", e);
        }
    }

    // ==========================
    // 🔧 helpers
    // ==========================

    private void createRow(Sheet sheet, int rowIdx, String key, String value, CellStyle style) {
        Row row = sheet.createRow(rowIdx);
        createCell(row, 0, key, style);
        createCell(row, 1, value, style);
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);

        return style;
    }

    private CellStyle createValueStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private String formatMoney(Double value) {
        if (value == null) return "0 ₽";
        return String.format("%,.0f ₽", value);
    }

    private String buildFileName(LocalDate start, LocalDate end) {
        return "сумма_за_период_" +
                start.format(DATE_FORMAT) +
                "_" +
                end.format(DATE_FORMAT) +
                ".xlsx";
    }
}