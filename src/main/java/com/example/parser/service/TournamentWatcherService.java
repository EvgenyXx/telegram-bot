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

    public void watch(String url, Long telegramId, Long chatId, TelegramLongPollingBot bot) {
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
                // 🔥 1. ПРОВЕРКА БЛИЖАЙШИХ ТУРНИРОВ (сегодня + 2 дня)
                boolean foundInUpcoming = resultService.isPlayerInUpcoming(w.player.getName());

                if (foundInUpcoming && !w.notifiedUpcoming) {
                    messageService.send(
                            w.bot,
                            w.chatId,
                            "🔥 Ты играешь в ближайшие 2 дня!\nПроверь расписание"
                    );
                    w.notifiedUpcoming = true;
                }

                // 🔥 2. ПРОВЕРКА ТЕКУЩЕГО ТУРНИРА (парсинг страницы)
                ResultService.ParsedResult parsed = resultService.calculateAll(w.url);

                boolean found = tournamentResultService.processResults(
                        parsed.getResults(),
                        w.player,
                        parsed.getTournamentId(),
                        parsed.getNightBonus(),
                        parsed.isFinished()
                );

                if (found && !parsed.isFinished() && !w.notifiedStarted) {
                    String message = "🔥 Ты есть в турнире!\n\n" +
                            "📅 Турнир начался\n" +
                            "Проверь результаты";

                    messageService.send(w.bot, w.chatId, message);
                    w.notifiedStarted = true;
                }

                // ✅ 3. ЗАВЕРШЕНИЕ ТУРНИРА
                if (parsed.isFinished()) {
                    String message;

                    if (found) {
                        message = "✅ Турнир завершён!\n\n" +
                                formatter.formatResults(parsed.getResults(), parsed.getNightBonus()) +
                                "\n💾 Результат сохранён";
                    } else {
                        message = "⚠️ Турнир завершён\n\n" +
                                "Мы не нашли тебя в результатах\n" +
                                "Проверь правильность имени";
                    }

                    messageService.send(w.bot, w.chatId, message);
                    it.remove(); // удаляем из отслеживания
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

        boolean notifiedUpcoming = false;
        boolean notifiedStarted = false;

        public WatchingTournament(String url, Player player, Long chatId, TelegramLongPollingBot bot) {
            this.url = url;
            this.player = player;
            this.chatId = chatId;
            this.bot = bot;
        }
    }
}