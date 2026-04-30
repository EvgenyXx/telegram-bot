package com.example.parser.modules.player.exception;

import com.example.parser.modules.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class BadCredentialsException extends BaseException {
    public BadCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "Неверный email или пароль");
    }
}