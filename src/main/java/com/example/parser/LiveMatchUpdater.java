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

                Integer messageId = liveMatchService.getMessageId(chatId);

                try {
                    if (messageId != null) {
                        view.update(chatId, bot, data, messageId);
                    } else {
                        throw new RuntimeException("no messageId");
                    }
                } catch (Exception e) {
                    // 💥 сообщение умерло → создаём новое
                    Integer newMessageId = view.renderAndReturnMessageId(chatId, bot, data);
                    liveMatchService.setMessageId(chatId, newMessageId);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}