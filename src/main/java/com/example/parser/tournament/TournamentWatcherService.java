//package com.example.parser.tournament;
//
//import com.example.parser.bot.BotHolder;
//import com.example.parser.domain.entity.PlayerNotification;
//import com.example.parser.notification.MessageService;
//import com.example.parser.notification.PlayerNotificationRepository;
//import com.example.parser.notification.formatter.TournamentMessageFormatter;
//import com.example.parser.player.Player;
//import com.example.parser.player.PlayerService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.telegram.telegrambots.bots.TelegramLongPollingBot;
//
//import java.time.LocalDate;
//import java.util.List;
//
////@Service
//// будем удалять
//@RequiredArgsConstructor
//@Slf4j
//public class TournamentWatcherService {
//
//    private final ResultService resultService;
//    private final TournamentResultService tournamentResultService;
//    private final PlayerService playerService;
//    private final MessageService messageService;
//    private final TournamentMessageFormatter formatter;
//    private final BotHolder botHolder;
//    private final PlayerNotificationRepository notificationRepo;
//
////    @Scheduled(fixedDelay = 300000)
//    public void check() {
//
//        log.warn("⏰ WATCHER START");
//
//        TelegramLongPollingBot bot = botHolder.getBot();
//        if (bot == null) {
//            log.warn("❌ Bot not ready");
//            return;
//        }
//
//        // ✅ берём ВСЕ НЕЗАВЕРШЕННЫЕ
//        List<PlayerNotification> list = notificationRepo.findByFinishedFalse();
//
//        for (PlayerNotification pn : list) {
//
//            try {
//
//                if (pn.getLink() == null) continue;
//
//                log.warn("📥 Проверяем {}", pn.getLink());
//
//                ResultService.ParsedResult parsed =
//                        resultService.calculateAll(pn.getLink());
//
//                boolean started = parsed.getResults() != null
//                        && !parsed.getResults().isEmpty();
//
//                // ❗ ещё не стартовал
//                if (!started) continue;
//
//                Player player =
//                        playerService.getByTelegramId(pn.getTelegramId());
//
//                if (player == null) continue;
//
//                boolean found = tournamentResultService.processResults(
//                        parsed.getResults(),
//                        player,
//                        parsed.getTournamentId(),
//                        parsed.getNightBonus(),
//                        parsed.isFinished()
//                );
//
//                // 🚀 СТАРТ
//                if (found
//                        && !parsed.isFinished()
//                        && !Boolean.TRUE.equals(pn.getStarted())) {
//
//                    log.warn("🚀 START DETECTED {}", pn.getTournamentId());
//
//                    messageService.send(
//                            bot,
//                            pn.getTelegramId(),
//                            "🔥 Ты есть в турнире!\n\n📅 Турнир начался"
//                    );
//
//                    pn.setStarted(true);
//                    notificationRepo.save(pn);
//                }
//
//                // ✅ ФИНИШ
//                if (parsed.isFinished()
//                        && !Boolean.TRUE.equals(pn.getFinished())) {
//
//                    log.warn("✅ FINISHED {}", pn.getTournamentId());
//
//                    String message = formatter.formatFinalWithPlayer(
//                            parsed.getResults(),
//                            parsed.getNightBonus(),
//                            player.getName()
//                    );
//
//                    messageService.send(bot, pn.getTelegramId(), message);
//
//                    pn.setFinished(true);
//                    notificationRepo.save(pn);
//                }
//
//            } catch (Exception e) {
//                log.error("❌ ERROR {}", pn.getLink(), e);
//            }
//        }
//    }
//}