package com.example.parser.modules.player.service.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VerificationMailStrategy implements MailStrategy {

    private final MailProperties mailProperties;

    @Override
    public String getType() {
        return "verification";
    }

    @Override
    public SimpleMailMessage createMessage(String to, Object... args) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getFrom());
        message.setTo(to);
        message.setSubject(mailProperties.getVerification().getSubject());
        message.setText(String.format(mailProperties.getVerification().getText(), args));
        return message;
    }
}