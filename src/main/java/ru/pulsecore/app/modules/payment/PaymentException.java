package ru.pulsecore.app.modules.payment;

import ru.pulsecore.app.modules.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PaymentException extends BaseException {
    public PaymentException(String message) {
        super(HttpStatus.BAD_GATEWAY, message);
    }
}