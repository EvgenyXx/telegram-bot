package com.example.parser.modules.player.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("pulsecore@yandex.ru");
        message.setTo(to);
        message.setSubject("PulseCore — Код подтверждения");
        message.setText("Ваш код подтверждения: " + code + "\n\nВведите его в приложении.");
        mailSender.send(message);
        log.info("📧 Код отправлен на {}", to);//todo убрать весь хард код
    }
}