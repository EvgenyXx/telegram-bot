package ru.pulsecore.app.modules.player.service.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordResetMailStrategy implements MailStrategy {

    private final MailProperties mailProperties;

    @Override
    public String getType() {
        return MailTypes.PASSWORD_RESET;
    }

    @Override
    public SimpleMailMessage createMessage(String to, Object... args) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getFrom());
        message.setTo(to);
        message.setSubject(mailProperties.getReset().getSubject());
        message.setText(String.format(mailProperties.getReset().getText(), args));
        return message;
    }
}