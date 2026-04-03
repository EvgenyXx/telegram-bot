package com.example.parser.tournament;

import com.example.parser.bot.BotHolder;
import com.example.parser.player.Player;
import com.example.parser.notification.MessageService;
import com.example.parser.notification.PlayerNotificationRepository;
import com.example.parser.notification.formatter.TournamentMessageFormatter;
import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.player.PlayerService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentWatcherService {

    private final ResultService resultService;
    private final TournamentResultService tournamentResultService;
    private final PlayerService playerService;
    private final MessageService messageService;
    private final TournamentMessageFormatter formatter;
    private final BotHolder botHolder;
    private final PlayerNotificationRepository notificationRepo;

    private final Map<String, WatchingTournament> active = new HashMap<>();

    // 🚀 ВОССТАНОВЛЕНИЕ после рестарта
    @PostConstruct
    public void init() {
        log.warn("♻️ Восстанавливаем watcher из БД");

        List<PlayerNotification> list = notificationRepo.findAll();

        for (PlayerNotification pn : list) {
            if (!Boolean.TRUE.equals(pn.getFinished())) {
                watch(pn.getLink(), pn.getTelegramId(), pn.getTelegramId());
            }
        }
    }

    // 🚀 запуск слежения
    public void watch(String url, Long telegramId, Long chatId) {

        if (active.containsKey(url)) {
            return;
        }

        Player player = playerService.getByTelegramId(telegramId);
        if (player == null) return;

        log.warn("👀 [WATCHER] Добавлен турнир: {}", url);

        active.put(url, new WatchingTournament(url, player, chatId));
    }

    // 🔁 каждые 5 минут
    @Scheduled(fixedDelay = 300000)
    public void check() {

        log.warn("⏰ [WATCHER] Запуск проверки. Активных турниров: {}", active.size());

        TelegramLongPollingBot bot = botHolder.getBot();
        if (bot == null) {
            log.warn("❌ Bot is not initialized yet");
            return;
        }

        Iterator<Map.Entry<String, WatchingTournament>> it = active.entrySet().iterator();

        while (it.hasNext()) {

            WatchingTournament w = it.next().getValue();

            try {
                log.warn("📥 [WATCHER] Проверяем турнир: {}", w.url);

                ResultService.ParsedResult parsed = resultService.calculateAll(w.url);

                log.warn("📊 [WATCHER] tournamentId={}", parsed.getTournamentId());
                log.warn("👥 [WATCHER] players={}", parsed.getResults().size());
                log.warn("🏁 [WATCHER] finished={}", parsed.isFinished());

                boolean found = tournamentResultService.processResults(
                        parsed.getResults(),
                        w.player,
                        parsed.getTournamentId(),
                        parsed.getNightBonus(),
                        parsed.isFinished()
                );

                // 🚀 уведомление о старте
                if (found && !parsed.isFinished() && !w.notifiedStarted) {

                    log.warn("🚀 [WATCHER] Турнир начался для игрока");

                    messageService.send(bot, w.chatId,
                            "🔥 Ты есть в турнире!\n\n📅 Турнир начался\nПроверь результаты");

                    w.notifiedStarted = true;
                }

                // ✅ завершение турнира
                if (parsed.isFinished()) {

                    log.warn("✅ [WATCHER] Турнир завершен → отправка результата");

                    String message = formatter.formatFinalWithPlayer(
                            parsed.getResults(),
                            parsed.getNightBonus(),
                            w.player.getName()
                    );

                    messageService.send(bot, w.chatId, message);

                    PlayerNotification pn =
                            notificationRepo.findByTournamentId(parsed.getTournamentId());

                    if (pn != null) {
                        pn.setFinished(true);
                        notificationRepo.save(pn);
                    }

                    it.remove();
                }

            } catch (Exception e) {
                log.error("❌ [WATCHER] Ошибка при проверке турнира {}", w.url, e);
            }
        }
    }

    // 📦 внутренняя модель
    private static class WatchingTournament {
        String url;
        Player player;
        Long chatId;

        boolean notifiedStarted = false;

        public WatchingTournament(String url, Player player, Long chatId) {
            this.url = url;
            this.player = player;
            this.chatId = chatId;
        }
    }
}