package ru.pulsecore.app.modules.player.service.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TournamentResultMailStrategy implements MailStrategy {

    private final MailProperties mailProperties;

    @Override
    public String getType() {
        return "tournament_result";
    }

    @Override
    public SimpleMailMessage createMessage(String to, Object... args) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getFrom());
        message.setTo(to);
        message.setSubject("Результаты турнира");
        message.setText(String.format("Ваш результат: %s", args));
        return message;
    }
}