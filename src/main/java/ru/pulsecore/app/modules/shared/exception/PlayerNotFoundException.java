package ru.pulsecore.app.modules.shared.exception;

import org.springframework.http.HttpStatus;

public class PlayerNotFoundException extends BaseException {
    public PlayerNotFoundException(String playerId) {
        super(HttpStatus.NOT_FOUND, "Игрок не найден: " + playerId);
    }
}