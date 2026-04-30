package com.example.parser.modules.player.service.strategy;

import org.springframework.mail.SimpleMailMessage;

public interface MailStrategy {
    String getType();
    SimpleMailMessage createMessage(String to, Object... args);
}