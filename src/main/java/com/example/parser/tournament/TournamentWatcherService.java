package com.example.parser.tournament;

import com.example.parser.player.Player;
import com.example.parser.notification.formatter.TournamentMessageFormatter;
import com.example.parser.notification.MessageService;
import com.example.parser.player.PlayerService;
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

    public void watch(String url, Long telegramId, Long chatId) {
        Player player = playerService.getByTelegramId(telegramId);
        if (player == null) return;

        active.put(url, new WatchingTournament(url, player, chatId));
    }

    @Scheduled(fixedDelay = 300000)
    public void check() {

        System.out.println("🔥 CHECK ЗАПУЩЕН: " + java.time.LocalDate.now());

        Iterator<Map.Entry<String, WatchingTournament>> it = active.entrySet().iterator();

        while (it.hasNext()) {
            WatchingTournament w = it.next().getValue();

            try {

                // 🔍 ЛОГ — ЧТО ИЩЕМ
                String searchName = w.player.getName().trim();

                System.out.println("🔍 ИЩЕМ: [" + searchName + "]");

                boolean foundInUpcoming = resultService.isPlayerInUpcoming(searchName);

                if (foundInUpcoming && !w.notifiedUpcoming) {
                    messageService.send(
                            w.chatId,
                            "🔥 Ты играешь в ближайшие 2 дня!\nПроверь расписание"
                    );
                    w.notifiedUpcoming = true;
                }

                // 🔥 2. ТЕКУЩИЙ ТУРНИР
                ResultService.ParsedResult parsed = resultService.calculateAll(w.url);

                boolean found = tournamentResultService.processResults(
                        parsed.getResults(),
                        w.player,
                        parsed.getTournamentId(),
                        parsed.getNightBonus(),
                        parsed.isFinished()
                );

                if (found && !parsed.isFinished() && !w.notifiedStarted) {
                    messageService.send(
                            w.chatId,
                            "🔥 Ты есть в турнире!\n\n📅 Турнир начался\nПроверь результаты"
                    );
                    w.notifiedStarted = true;
                }

                // ✅ ЗАВЕРШЕНИЕ
                if (parsed.isFinished()) {

                    String message = formatter.formatFinalWithPlayer(
                            parsed.getResults(),
                            parsed.getNightBonus(),
                            w.player.getName()
                    );

                    messageService.send(w.chatId, message);

                    it.remove();
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


        boolean notifiedUpcoming = false;
        boolean notifiedStarted = false;

        public WatchingTournament(String url, Player player, Long chatId) {
            this.url = url;
            this.player = player;
            this.chatId = chatId;

        }
    }
}