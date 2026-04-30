package com.example.parser.modules.shared.exception;

import com.example.parser.modules.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class TournamentParseException extends BaseException {
    public TournamentParseException(String url, Exception cause) {
        super(HttpStatus.BAD_REQUEST, "Ошибка парсинга турнира: " + cause.getMessage());
    }
}