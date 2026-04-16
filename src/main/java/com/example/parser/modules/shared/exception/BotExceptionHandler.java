package com.example.parser.modules.shared.exception;

import com.example.parser.modules.notification.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotExceptionHandler {

    private final MessageService messageService;

    public void handle(Exception e, TelegramLongPollingBot bot, Long chatId) {

        // бизнес ошибки (наши кастомные)
        if (e instanceof BusinessException be) {
            messageService.send(bot, chatId, be.getMessage());
            return;
        }

        // всё остальное — системка
        log.error("Unexpected error", e);

        messageService.send(bot, chatId,
                "❌ Произошла ошибка. Попробуй позже");
    }
}