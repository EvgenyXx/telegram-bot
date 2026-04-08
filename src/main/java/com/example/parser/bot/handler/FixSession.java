package com.example.parser.bot.handler;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FixSession {
    private Long playerId;
    private Long tournamentId;
}