package com.example.parser.modules.player.exception;

import com.example.parser.modules.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PlayerNameAlreadyExistsException extends BaseException {
    public PlayerNameAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "Игрок с таким именем уже существует");
    }
}