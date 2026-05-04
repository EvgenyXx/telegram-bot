package ru.pulsecore.app.modules.player.exception;

import ru.pulsecore.app.modules.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class SamePasswordException extends BaseException {
    public SamePasswordException() {
        super(HttpStatus.BAD_REQUEST, "Новый пароль не должен совпадать со старым");
    }
}