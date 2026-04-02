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
    private final MessageService messageService;
    private final TournamentMessageFormatter formatter;

    private final Map<String, WatchingTournament> active = new HashMap<>();

    public void watch(String url, Long telegramId, Long chatId, TelegramLongPollingBot bot){
        Player player = playerService.getByTelegramId(telegramId);
        if (player == null) return;

        active.put(url, new WatchingTournament(url, player, chatId, bot));
    }

    @Scheduled(fixedDelay = 300000) // каждые 5 минут
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

                // 🔥 НОВОЕ: уведомление о том что ты есть в турнире
                if (found && !parsed.isFinished() && !w.notified) {
                    String message = "🔥 Ты есть в турнире!\n\n"
                            + "📅 Турнир скоро начнётся\n"
                            + "Проверь расписание";

                    messageService.send(w.bot, w.chatId, message);

                    w.notified = true;
                }

                // ✅ СТАРОЕ: уведомление о завершении
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

                    messageService.send(w.bot, w.chatId, message);

                    it.remove(); // удаляем после завершения
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class WatchingTournament {
        String url;
        Player player;
        Long chatId;
        TelegramLongPollingBot bot;
        boolean notified = false;

        public WatchingTournament(String url, Player player, Long chatId, TelegramLongPollingBot bot) {
            this.url = url;
            this.player = player;
            this.chatId = chatId;
            this.bot = bot;
        }
    }
}