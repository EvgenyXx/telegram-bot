package com.example.parser.modules.payment;

import com.example.parser.modules.shared.exception.BaseException;
import org.springframework.http.HttpStatus;

public class PaymentException extends BaseException {
    public PaymentException(String message) {
        super(HttpStatus.BAD_GATEWAY, message);
    }
}