package com.example.parser.modules.player.exception;

import com.example.parser.modules.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class BadResetCodeException extends BaseException {
    public BadResetCodeException() {
        super(HttpStatus.BAD_REQUEST, "Неверный код сброса");
    }
}