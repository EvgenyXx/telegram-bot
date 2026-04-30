package com.example.parser.modules.player.exception;

import com.example.parser.modules.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class OldPasswordMismatchException extends BaseException {
    public OldPasswordMismatchException() {
        super(HttpStatus.BAD_REQUEST, "Старый пароль не совпадает");
    }
}