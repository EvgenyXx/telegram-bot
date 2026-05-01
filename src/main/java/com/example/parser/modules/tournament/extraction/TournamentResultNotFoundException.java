package com.example.parser.modules.tournament.extraction;

import com.example.parser.modules.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class TournamentResultNotFoundException extends BaseException {
    public TournamentResultNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "Результат турнира не найден: " + id);
    }
}