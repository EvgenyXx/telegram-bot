package com.example.parser.modules.tournament.calendar.service;

import com.example.parser.modules.tournament.calendar.domain.CalendarSession;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CalendarSessionService {

    private final Map<Long, CalendarSession> sessions = new ConcurrentHashMap<>();

    public CalendarSession get(Long chatId) {
        return sessions.computeIfAbsent(chatId, id -> new CalendarSession());
    }

    public void remove(Long chatId) {
        sessions.remove(chatId);
    }
}