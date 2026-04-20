package com.example.parser.modules.match.service;

import com.example.parser.modules.match.dto.LiveMatchData;
import com.example.parser.core.dto.ResultDto;
import com.example.parser.modules.match.client.LiveMatchFetcher;
import com.example.parser.modules.match.api.LiveMatchView;
import com.example.parser.modules.tournament.service.result.ParsedResult;
import com.example.parser.modules.tournament.service.result.ResultService;
import com.example.parser.modules.tournament.service.result.TournamentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class LiveMatchUpdater {

    private final LiveMatchService liveMatchService;
    private final LiveMatchFetcher fetcher;
    private final LiveMatchView view;
    private final ResultService resultService;

    @Async
    public void start(Long chatId, TelegramLongPollingBot bot) {

        int tick = 0;
        Map<String, Integer> lastProfit = new HashMap<>();

        while (liveMatchService.isAutoUpdating(chatId)) {
            try {
                TimeUnit.SECONDS.sleep(20);

                String link = liveMatchService.getLink(chatId);
                if (link == null) continue;

                LiveMatchData data = fetcher.fetch(link);

                // 🏁 завершение турнира
                if (data.getStatus() == TournamentStatus.FINISHED) {
                    view.render(chatId, bot, data);
                    return;
                }

                // 💰 считаем раз в 15 сек
                tick++;
                if (tick % 30 == 0) {
                    ParsedResult parsed = resultService.calculateAll(link);

                    lastProfit.clear();
                    for (ResultDto dto : parsed.getResults()) {
                        lastProfit.put(dto.getPlayer(), dto.getTotal());
                    }
                }

                Integer messageId = liveMatchService.getMessageId(chatId);

                if (messageId == null) {
                    Integer newId = view.renderAndReturnMessageId(chatId, bot, data, lastProfit);
                    liveMatchService.setMessageId(chatId, newId);
                    continue;
                }

                try {
                    view.update(chatId, bot, data, messageId, lastProfit);
                } catch (Exception e) {
                    Integer newId = view.renderAndReturnMessageId(chatId, bot, data, lastProfit);
                    liveMatchService.setMessageId(chatId, newId);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}