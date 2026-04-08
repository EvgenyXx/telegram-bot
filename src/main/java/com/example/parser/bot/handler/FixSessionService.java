package com.example.parser.bot.handler;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FixSessionService {

    private final Map<Long, FixSession> sessions = new HashMap<>();

    public void create(Long chatId, Long playerId, Long tournamentId) {
        sessions.put(chatId, new FixSession(playerId, tournamentId));
    }

    public FixSession get(Long chatId) {
        return sessions.get(chatId);
    }

    public void remove(Long chatId) {
        sessions.remove(chatId);
    }
}