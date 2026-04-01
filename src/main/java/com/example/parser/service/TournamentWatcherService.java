package com.example.parser.service;

import com.example.parser.domain.entity.Player;
import com.example.parser.formatter.TournamentMessageFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TournamentWatcherService {

    private final ResultService resultService;
    private final TournamentResultService tournamentResultService;
    private final PlayerService playerService;
    private final MessageService messageService;              // 👈 ДОБАВИТЬ
    private final TournamentMessageFormatter formatter;       // 👈 ДОБАВИТЬ
    private final Map<String, WatchingTournament> active = new HashMap<>();


    public void watch(String url, Long telegramId, Long chatId, TelegramLongPollingBot bot){


        Player player = playerService.getByTelegramId(telegramId);
        if (player == null) return;

        active.put(url, new WatchingTournament(url, player, chatId, bot));
    }

    @Scheduled(fixedDelay = 300000) // 5 минут
    public void check() {
        Iterator<Map.Entry<String, WatchingTournament>> it = active.entrySet().iterator();

        while (it.hasNext()) {
            WatchingTournament w = it.next().getValue();

            try {
                ResultService.ParsedResult parsed = resultService.calculateAll(w.url);

                boolean found = tournamentResultService.processResults(
                        parsed.getResults(),
                        w.player,
                        parsed.getTournamentId(),
                        parsed.getNightBonus(),
                        parsed.isFinished()
                );

                if (parsed.isFinished()) {

                    String message;

                    if (found) {
                        message = "✅ Турнир завершён!\n\n"
                                + formatter.formatResults(parsed.getResults(), parsed.getNightBonus())
                                + "\n💾 Результат сохранён";
                    } else {
                        message = "⚠️ Турнир завершён\n\n"
                                + "Мы не нашли тебя в результатах\n"
                                + "Проверь правильность имени";
                    }

                    messageService.send(w.bot, w.chatId, message);  // 👈 ВОТ ОНО

                    it.remove(); // удаляем после отправки
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class WatchingTournament {
        String url;
        Player player;
        Long chatId;                          // 👈 ДОБАВИТЬ
        TelegramLongPollingBot bot;           // 👈 ДОБАВИТЬ

        public WatchingTournament(String url, Player player, Long chatId, TelegramLongPollingBot bot) {
            this.url = url;
            this.player = player;
            this.chatId = chatId;
            this.bot = bot;
        }
    }
}