package com.example.parser.modules.player.exception;

import com.example.parser.modules.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class MailStrategyNotFoundException extends BaseException {
    public MailStrategyNotFoundException(String type) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "Почтовая стратегия не найдена: " + type);
    }
}