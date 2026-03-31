package com.example.parser.bot.handler;

import com.example.parser.LiveMatchFetcher;
import com.example.parser.LiveMatchUpdater;
import com.example.parser.LiveMatchView;
import com.example.parser.domain.dto.LiveMatchData;
import com.example.parser.service.LiveMatchService;
import com.example.parser.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@Component
@RequiredArgsConstructor
public class LiveMatchHandler {

    private final LiveMatchService liveMatchService;
    private final MessageService messageService;
    private final LiveMatchUpdater updater;
    private final LiveMatchFetcher fetcher;
    private final LiveMatchView view;

    // 🚀 старт лайва
    public void start(Long chatId, TelegramLongPollingBot bot) throws Exception {
        handleLiveMatch(chatId, bot);
    }

    // 🛑 стоп лайва
    public void stop(Long chatId, TelegramLongPollingBot bot) {
        liveMatchService.clear(chatId);
        liveMatchService.clearMessageId(chatId);
        liveMatchService.stopAutoUpdate(chatId);
        liveMatchService.stopWaiting(chatId);
        liveMatchService.clearLastMessage(chatId);

        messageService.send(bot, chatId, "🚪 Вы вышли из лайва");
    }

    // ⏳ ждём ссылку
    public void waitForLink(Long chatId, TelegramLongPollingBot bot) {
        liveMatchService.startWaiting(chatId);
        messageService.send(bot, chatId, "Скинь ссылку на турнир");
    }

    // 🔗 получили ссылку
    public void handleLink(Long chatId, String link, TelegramLongPollingBot bot) throws Exception {
        liveMatchService.setLink(chatId, link);
        messageService.send(bot, chatId, "🔥 Трансляция запущена");

        handleLiveMatch(chatId, bot);
    }

    // 🔥 главный метод
    public void handleLiveMatch(Long chatId, TelegramLongPollingBot bot) throws Exception {

        String link = liveMatchService.getLink(chatId);

        // нет ссылки
        if (link == null) {
            if (!liveMatchService.isWaiting(chatId)) return;

            messageService.send(bot, chatId, "Скинь ссылку на турнир");
            return;
        }

        // запуск автообновления (1 раз)
        if (!liveMatchService.isAutoUpdating(chatId)) {
            liveMatchService.startAutoUpdate(chatId);
            updater.start(chatId, bot);
        }

        // 🔥 получаем данные
        LiveMatchData data = fetcher.fetch(link);

        // 🔥 отображаем
        view.render(chatId, bot, data);
    }

    public void info(Long chatId, TelegramLongPollingBot bot) throws Exception {

        String link = liveMatchService.getLink(chatId);

        if (link == null) {
            messageService.send(bot, chatId, "Сначала запусти трансляцию");
            return;
        }

        LiveMatchData data = fetcher.fetch(link);

        view.render(chatId, bot, data); // 🔥 ВАЖНО: view, а не liveMatchView
    }
}