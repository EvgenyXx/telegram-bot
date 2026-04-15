package com.example.parser.lineup;

import com.example.parser.domain.entity.Lineup;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class LineupMessageBuilder {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    public InputFile buildTomorrowFile(List<Lineup> lineups) {
        if (lineups == null || lineups.isEmpty()) {
            return null;
        }

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Составы");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle playerStyle = createPlayerStyle(workbook);

            int rowIdx = 0;

            // 📅 дата
            LocalDate tomorrow = LocalDate.now(ZONE).plusDays(1);

            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("📋 Составы — " + tomorrow.format(DATE_FORMAT));
            titleCell.setCellStyle(headerStyle);

            rowIdx++;

            // 🔥 сортировка по времени
            List<Lineup> sorted = lineups.stream()
                    .sorted(Comparator.comparing(Lineup::getTime))
                    .toList();

            for (Lineup l : sorted) {

                // 👉 заголовок блока
                Row headerRow = sheet.createRow(rowIdx++);
                Cell headerCell = headerRow.createCell(0);

                String headerText = "⏰ " + l.getTime() + " | Лига " + l.getLeague();
                headerCell.setCellValue(headerText);
                headerCell.setCellStyle(headerStyle);

                // 👉 разделитель
                Row divider = sheet.createRow(rowIdx++);
                divider.createCell(0).setCellValue("-------------------------");

                // 👉 игроки (FIX: без lambda)
                String[] players = l.getPlayers().split(",");

                for (String playerRaw : players) {
                    String player = shortName(playerRaw.trim());

                    Row row = sheet.createRow(rowIdx++);
                    Cell cell = row.createCell(0);
                    cell.setCellValue(player);
                    cell.setCellStyle(playerStyle);
                }

                rowIdx++; // отступ между блоками
            }

            sheet.autoSizeColumn(0);

            workbook.write(out);

            return new InputFile(
                    new ByteArrayInputStream(out.toByteArray()),
                    buildFileName()
            );

        } catch (Exception e) {
            throw new RuntimeException("Ошибка создания Excel составов", e);
        }
    }

    // ==========================
    // 🔧 стили
    // ==========================

    private CellStyle createHeaderStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        return style;
    }

    private CellStyle createPlayerStyle(Workbook wb) {
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 11);

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        return style;
    }

    // ==========================
    // 📄 имя файла
    // ==========================

    private String buildFileName() {
        LocalDate tomorrow = LocalDate.now(ZONE).plusDays(1);
        return "составы_" + tomorrow.format(DATE_FORMAT) + ".xlsx";
    }

    // ==========================
    // 👤 формат имени
    // ==========================

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