package ru.pulsecore.app.modules.player.exception;

import ru.pulsecore.app.modules.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PlayerNameAlreadyExistsException extends BaseException {
    public PlayerNameAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "Игрок с таким именем уже существует");
    }
}