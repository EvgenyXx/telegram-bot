package com.example.parser.modules.shared.exception;

import com.example.parser.modules.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PlayerNotFoundException extends BaseException {
    public PlayerNotFoundException(String playerId) {
        super(HttpStatus.NOT_FOUND, "Игрок не найден: " + playerId);
    }
}