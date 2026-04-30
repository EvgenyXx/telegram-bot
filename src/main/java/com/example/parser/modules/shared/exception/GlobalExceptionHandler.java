package com.example.parser.modules.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBase(BaseException e) {
        ErrorResponse response = ErrorResponse.builder()
                .status(e.getStatus().value())
                .error(e.getStatus().getReasonPhrase())
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(e.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception e) {
        log.error("Внутренняя ошибка", e);  // ← добавь эту строку
        ErrorResponse response = ErrorResponse.builder()

                .status(500)
                .error("Internal Server Error")
                .message("Внутренняя ошибка сервера")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(500).body(response);
    }


}