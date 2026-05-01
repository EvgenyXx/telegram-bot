package com.example.parser.modules.shared.exception;


import org.springframework.http.HttpStatus;

public class TournamentNotFoundException extends BaseException {
    public TournamentNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "Турнир не найден: " + id);
    }
}