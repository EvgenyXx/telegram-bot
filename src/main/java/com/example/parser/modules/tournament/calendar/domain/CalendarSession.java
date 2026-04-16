package com.example.parser.modules.tournament.calendar.domain;

import lombok.Data;

import java.time.LocalDate;
import java.time.YearMonth;

@Data
public class CalendarSession {
    private CalendarState state;
    private Long playerId;
    private LocalDate start;
    private LocalDate end;
    private YearMonth currentMonth;
    private Integer messageId;
    private Long telegramId;
    private Long tournamentId;
}