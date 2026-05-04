package ru.pulsecore.app.modules.shared.exception;

import org.springframework.http.HttpStatus;

public class SiteUnavailableException extends BaseException {
    public SiteUnavailableException() {
        super(HttpStatus.SERVICE_UNAVAILABLE, "Сайт недоступен, попробуйте позже");
    }
}