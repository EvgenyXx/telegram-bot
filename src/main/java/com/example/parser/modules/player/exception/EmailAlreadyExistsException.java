package com.example.parser.modules.player.exception;

import com.example.parser.modules.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BaseException {
    public EmailAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "Email уже используется");
    }
}