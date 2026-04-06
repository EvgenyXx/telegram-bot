//package com.example.parser.bot.handler;
//
//import com.example.parser.config.AdminProperties;
//import com.example.parser.domain.dto.FullStatsDto;
//import com.example.parser.player.Player;
//import com.example.parser.stats.StatsFormatter;
//import com.example.parser.match.LiveMatchService;
//import com.example.parser.notification.MessageService;
//import com.example.parser.player.PlayerService;
//import com.example.parser.tournament.TournamentResultService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.telegram.telegrambots.bots.TelegramLongPollingBot;
//import org.telegram.telegrambots.meta.api.objects.Update;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class TextHandler {
//
//    private final StartHandler startHandler;
//    private final RegisterHandler registerHandler;
//    private final TournamentHandler tournamentHandler;
//    private final AdminHandler adminHandler;
//    private final MessageService messageService;
//    private final PlayerService playerService;
//    private final TournamentResultService tournamentResultService;
//    private final StatsFormatter statsFormatter;
//    private final AdminProperties adminProperties;
//    private final LiveMatchHandler liveMatchHandler;
//    private final LiveMatchService liveMatchService;
//
//    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {
//
//        String text = update.getMessage().getText();
//        Long chatId = update.getMessage().getChatId();
//        Long telegramId = update.getMessage().getFrom().getId();
//
//        log.info("🔥 NEW UPDATE: text='{}', chatId={}, telegramId={}", text, chatId, telegramId);
//
//        Player player = playerService.getByTelegramId(telegramId);
//
//        log.info("👤 PLAYER: {}", player == null ? "NULL" : player.getName());
//
//        // 🚫 блок
//        if (player != null && player.isBlocked()) {
//            log.warn("⛔ USER BLOCKED");
//            messageService.send(bot, chatId, "🚫 Ты заблокирован");
//            return;
//        }
//
//        // ===== START =====
//        if (text.equals("/start")) {
//            log.info("➡️ /start command");
//
//            if (player == null) {
//                log.info("🆕 USER NOT FOUND → startHandler");
//                startHandler.handle(update, bot);
//            } else {
//                log.info("✅ EXISTING USER");
//                messageService.send(bot, chatId, "С возвращением, " + player.getName());
//                messageService.sendMenu(bot, chatId, telegramId, null);
//            }
//            return;
//        }
//
//        // 🔥 РЕГИСТРАЦИЯ
//        if (player == null) {
//            log.info("🆕 GO TO REGISTER HANDLER");
//            registerHandler.handle(update, bot);
//            return;
//        }
//
//        log.info("✅ REGISTERED USER FLOW");
//
//        // ===== USER =====
//        if (text.equals("📅 Мои турниры")) {
//            log.info("📅 tournaments");
//            adminHandler.openCalendar(chatId, telegramId, "USER_TOURNAMENTS", bot);
//            return;
//        }
//
//        if (text.equals("💰 Сумма за период")) {
//            log.info("💰 sum");
//            adminHandler.openCalendar(chatId, telegramId, "USER_SUM", bot);
//            return;
//        }
//
//        if (text.equals("📊 Моя статистика")) {
//            log.info("📊 stats");
//            FullStatsDto stats = tournamentResultService.getFullStats(player);
//            messageService.send(bot, chatId, statsFormatter.formatFullStats(stats));
//            return;
//        }
//
//        // ===== LINK =====
//        if (text.startsWith("http")) {
//            log.info("🔗 link detected");
//            tournamentHandler.handle(update, bot);
//            return;
//        }
//
//        log.warn("🤷 UNKNOWN COMMAND");
//        messageService.send(bot, chatId, "Неизвестная команда 🤷‍♂️");
//        messageService.sendMenu(bot, chatId, telegramId, null);
//    }
//}