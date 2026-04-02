package com.example.parser.test;

import java.time.*;

public class TestApiMain {

    public static void main(String[] args) {

        // 👇 ВРУЧНУЮ задаём как будто из API
//        String datePart = "2026-04-04";
//        String timePart = "18:00";
//
//        // 👇 вызываем проверку
//        checkTournamentTime(datePart, timePart);
        System.out.println("ZONED NOW: " + java.time.ZonedDateTime.now());
    }

    // 🔥 ЛОГИКА КАК В ТВОЕМ SCHEDULER + ПОЛНЫЙ DEBUG
    public static void checkTournamentTime(String datePart, String timePart) {
        try {
            // 🧠 текущее время сервера
            LocalDateTime nowSystem = LocalDateTime.now();

            // 🇷🇺 московское время
            ZonedDateTime nowMoscow = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));

            // 📅 время турнира
            LocalDateTime tournamentTime = LocalDateTime.of(
                    LocalDate.parse(datePart),
                    LocalTime.parse(timePart)
            );

            // ⏳ напоминание (-3 часа)
            LocalDateTime reminderTime = tournamentTime.minusHours(3);

            System.out.println("\n============= DEBUG TIME =============");
            System.out.println("🖥 SYSTEM NOW:        " + nowSystem);
            System.out.println("🇷🇺 MOSCOW NOW:       " + nowMoscow);
            System.out.println("📅 API DATE:         " + datePart);
            System.out.println("⏰ API TIME:         " + timePart);
            System.out.println("🚀 TOURNAMENT TIME:  " + tournamentTime);
            System.out.println("⏳ REMINDER TIME:    " + reminderTime);

            System.out.println("\n--- CHECKS ---");

            boolean afterReminder = nowSystem.isAfter(reminderTime);
            boolean beforeTournament = nowSystem.isBefore(tournamentTime);

            System.out.println("NOW > REMINDER ?      " + afterReminder);
            System.out.println("NOW < TOURNAMENT ?    " + beforeTournament);

            if (afterReminder && beforeTournament) {
                System.out.println("✅ НАПОМИНАНИЕ ДОЛЖНО СРАБОТАТЬ");
            }

            if (nowSystem.isAfter(tournamentTime)) {
                System.out.println("🔥 ТУРНИР УЖЕ НАЧАЛСЯ");
            } else {
                System.out.println("❌ ТУРНИР ЕЩЁ НЕ НАЧАЛСЯ");
            }

            System.out.println("=====================================\n");

        } catch (Exception e) {
            System.out.println("❌ ОШИБКА: " + e.getMessage());
        }
    }
}