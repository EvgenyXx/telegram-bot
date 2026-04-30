package com.example.parser.modules.player.service.strategy;

import com.example.parser.modules.player.exception.MailStrategyNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MailStrategyRegistry {

    private final Map<String, MailStrategy> strategies;
    private final JavaMailSender mailSender;

    public MailStrategyRegistry(List<MailStrategy> strategyList, JavaMailSender mailSender) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(MailStrategy::getType, s -> s));
        this.mailSender = mailSender;
    }

    public void send(String type, String to, Object... args) {
        MailStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new MailStrategyNotFoundException(type);
        }
        mailSender.send(strategy.createMessage(to, args));
        log.info("📧 {} отправлен на {}", type, to);
    }
}