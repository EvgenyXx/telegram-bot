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

        Integer messageId = liveMatchService.getMessageId(chatId);

        while (liveMatchService.isAutoUpdating(chatId)) {
            try {
                TimeUnit.SECONDS.sleep(5);

                String link = liveMatchService.getLink(chatId);
                if (link == null) continue;

                LiveMatchData data = fetcher.fetch(link);

                // 💥 НЕТ ЛАЙВА — НЕ ТРОГАЕМ ЧАТ
                if (data.getMatch() == null) {
                    continue;
                }

                // если нет сообщения — создаём
                if (messageId == null) {
                    messageId = view.renderAndReturnMessageId(chatId, bot, data);
                    liveMatchService.setMessageId(chatId, messageId);
                    continue;
                }

                // только edit
                try {
                    view.update(chatId, bot, data, messageId);
                } catch (Exception e) {
                    messageId = view.renderAndReturnMessageId(chatId, bot, data);
                    liveMatchService.setMessageId(chatId, messageId);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}