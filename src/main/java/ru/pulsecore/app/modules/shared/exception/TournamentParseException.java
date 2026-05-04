package ru.pulsecore.app.modules.shared.exception;

import org.springframework.http.HttpStatus;

public class TournamentParseException extends BaseException {
    public TournamentParseException(String url, Exception cause) {
        super(HttpStatus.BAD_REQUEST, "Ошибка парсинга турнира: " + cause.getMessage());
    }
}