package com.example.parser.notification;

import com.example.parser.bot.BotHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

//Чем занимается:
//📩 Отправляет сообщения пользователю в Telegram
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final MessageService messageService;
    private final BotHolder botHolder;

    public void send(Long telegramId, String text) {
        var bot = getBot();
        if (bot == null) return;

        messageService.send(bot, telegramId, text);
    }

    private TelegramLongPollingBot getBot() {
        var bot = botHolder.getBot();

        if (bot == null) {
            log.warn("Bot is not initialized");
        }

        return bot;
    }

    public void sendWithKeyboard(Long telegramId, String text, Long playerId, Long tournamentId) {
        var bot = getBot();
        if (bot == null) return;

        messageService.sendInlineKeyboard(
                bot,
                telegramId,
                text,
                "✏️ Исправить результат",
                "FIX_RESULT:" + playerId + ":" + tournamentId
        );
    }
}