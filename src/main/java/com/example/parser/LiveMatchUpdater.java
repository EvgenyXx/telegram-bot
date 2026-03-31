package com.example.parser;

import com.example.parser.bot.handler.LiveMatchHandler;
import com.example.parser.domain.dto.LiveMatchData;
import com.example.parser.service.LiveMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class LiveMatchUpdater {

    private final LiveMatchService liveMatchService;
    private final LiveMatchFetcher fetcher;
    private final LiveMatchView view;

    @Async
    public void start(Long chatId, TelegramLongPollingBot bot) {

        while (liveMatchService.isAutoUpdating(chatId)) {
            try {
                TimeUnit.SECONDS.sleep(5);

                String link = liveMatchService.getLink(chatId);
                if (link == null) continue;

                LiveMatchData data = fetcher.fetch(link);

                // ❌ НЕТ МАТЧА → НИЧЕГО НЕ ДЕЛАЕМ
                if (data.getMatch() == null) continue;

                Integer messageId = liveMatchService.getMessageId(chatId);

                // 🟢 если сообщения нет → создаём
                if (messageId == null) {
                    Integer newId = view.renderAndReturnMessageId(chatId, bot, data);
                    liveMatchService.setMessageId(chatId, newId);
                    continue;
                }

                // 🟡 пробуем обновить
                try {
                    view.update(chatId, bot, data, messageId);
                } catch (Exception e) {

                    // 💥 если сообщение умерло → создаём новое
                    Integer newId = view.renderAndReturnMessageId(chatId, bot, data);
                    liveMatchService.setMessageId(chatId, newId);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}