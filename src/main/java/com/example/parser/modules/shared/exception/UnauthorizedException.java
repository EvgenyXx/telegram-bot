package com.example.parser.modules.shared.exception;


import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException() {
        super(HttpStatus.UNAUTHORIZED, "Требуется авторизация");
    }
}