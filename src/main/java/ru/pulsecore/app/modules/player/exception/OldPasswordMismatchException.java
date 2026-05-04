package ru.pulsecore.app.modules.player.exception;

import ru.pulsecore.app.modules.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class OldPasswordMismatchException extends BaseException {
    public OldPasswordMismatchException() {
        super(HttpStatus.BAD_REQUEST, "Старый пароль не совпадает");
    }
}