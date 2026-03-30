package com.example.parser.bot.handler;

import com.example.parser.config.AdminProperties;
import com.example.parser.formatter.StatsFormatter;
import com.example.parser.dto.FullStatsDto;
import com.example.parser.entity.Player;
import com.example.parser.model.Match;
import com.example.parser.parser.MatchParser;
import com.example.parser.service.*;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageRouter {

    private final StartHandler startHandler;
    private final RegisterHandler registerHandler;
    private final TournamentHandler tournamentHandler;
    private final HistoryHandler historyHandler;
    private final AdminHandler adminHandler;
    private final MessageService messageService;
    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;
    private final StatsFormatter statsFormatter;
    private final AdminProperties adminProperties;
    private final LiveMatchService liveMatchService;
    private final MatchParser matchParser;

    private boolean isBlocked(Player player, Long chatId, TelegramLongPollingBot bot) {
        if (player != null && player.isBlocked()) {
            messageService.send(bot, chatId, "🚫 Ты заблокирован");
            return true;
        }
        return false;
    }

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        // ===== CALLBACK =====
        if (update.hasCallbackQuery()) {

            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Long telegramId = update.getCallbackQuery().getFrom().getId();

            bot.execute(new AnswerCallbackQuery(update.getCallbackQuery().getId()));

            Player player = playerService.getByTelegramId(telegramId);
            if (isBlocked(player, chatId, bot)) return;

            String data = update.getCallbackQuery().getData();

            if (data.startsWith("date_") || data.startsWith("month_") || data.equals("ignore")) {
                adminHandler.handleCalendarCallback(chatId, data, bot);
                return;
            }

            if (data.startsWith("player_")) {
                Long playerId = Long.parseLong(data.replace("player_", ""));
                adminHandler.handlePlayerSelected(chatId, playerId, bot);
                return;
            }

            // 🔥 LIVE MATCH (если когда-нибудь сделаешь inline кнопку)
            if (data.equals("live_match")) {
                handleLiveMatch(chatId, bot);
                return;
            }

            if (data.startsWith("block_user_")) {
                Long playerId = Long.parseLong(data.replace("block_user_", ""));
                Player target = playerService.findById(playerId);

                if (target != null && adminProperties.isAdmin(target.getTelegramId())) {
                    messageService.send(bot, chatId, "❌ Нельзя заблокировать администратора");
                    return;
                }

                playerService.block(playerId);
                messageService.send(bot, chatId, "🚫 Пользователь заблокирован");
                adminHandler.handlePlayerSelected(chatId, playerId, bot);
                return;
            }

            if (data.startsWith("unblock_user_")) {
                Long playerId = Long.parseLong(data.replace("unblock_user_", ""));
                playerService.unblock(playerId);
                messageService.send(bot, chatId, "✅ Пользователь разблокирован");
                adminHandler.handlePlayerSelected(chatId, playerId, bot);
                return;
            }

            if (data.equals("tournaments")) {
                adminHandler.openCalendar(chatId, telegramId, "PLAYER_TOURNAMENTS", bot);
                return;
            }

            if (data.equals("sum")) {
                adminHandler.openCalendar(chatId, telegramId, "PLAYER_SUM", bot);
                return;
            }

            return;
        }

        // ===== TEXT =====
        if (!(update.hasMessage() && update.getMessage().hasText())) return;

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        Player player = playerService.getByTelegramId(telegramId);
        if (isBlocked(player, chatId, bot)) return;

        // ===== LIVE LINK (ожидание ссылки) =====
        if (liveMatchService.isWaiting(chatId) && text.startsWith("http")) {
            liveMatchService.setLink(chatId, text);
            messageService.send(bot, chatId, "✅ Ссылка сохранена\nЖми 'Лайв матч'");
            return;
        }

        // 🔥 ВОТ ГЛАВНОЕ — ОБРАБОТКА КНОПКИ
        if (text.equals("🔥 Лайв матч")) {
            handleLiveMatch(chatId, bot);
            return;
        }

        // 🔥 INFO
        if (text.equals("/info")) {
            InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("🌐 Открыть турниры");
            button.setUrl("https://masters-league.com/tours-rus/");
            keyboard.setKeyboard(List.of(List.of(button)));

            messageService.sendInlineKeyboard(
                    bot,
                    chatId,
                    "ℹ️ Информация о турнирах:",
                    keyboard
            );
            return;
        }

        // ===== ADMIN STATE =====
        if (adminHandler.isInProgress(chatId)) {
            if (text.equals("📅 Мои турниры") ||
                    text.equals("💰 Сумма за период") ||
                    text.equals("📊 Моя статистика") ||
                    text.equals("/start") ||
                    text.equals("/info")) {

                adminHandler.reset(chatId);

            } else {
                adminHandler.handle(update, bot);
                return;
            }
        }

        // ===== ADMIN =====
        if (text.equals("📊 Статистика") && adminProperties.isAdmin(telegramId)) {
            adminHandler.handle(update, bot);
            return;
        }

        // ===== USER =====
        if (text.equals("📅 Мои турниры")) {
            adminHandler.openCalendar(chatId, telegramId, "USER_TOURNAMENTS", bot);
            return;
        }

        if (text.equals("💰 Сумма за период")) {
            adminHandler.openCalendar(chatId, telegramId, "USER_SUM", bot);
            return;
        }

        if (text.equals("📊 Моя статистика")) {
            if (player == null) {
                messageService.send(bot, chatId, "❌ Пользователь не найден");
                return;
            }

            FullStatsDto stats = tournamentResultService.getFullStats(player);
            String response = statsFormatter.formatFullStats(stats);
            messageService.send(bot, chatId, response);
            return;
        }

        // ===== START =====
        if (text.equals("/start")) {
            if (player == null) {
                startHandler.handle(update, bot);
            } else {
                messageService.send(bot, chatId, "С возвращением, " + player.getName());
                messageService.sendMenu(bot, chatId, telegramId);
            }
            return;
        }

        // ===== LINK =====
        if (text.startsWith("http")) {
            tournamentHandler.handle(update, bot);
            return;
        }

        // ===== FALLBACK =====
        if (player == null) {
            registerHandler.handle(update, bot);
        } else {
            messageService.send(bot, chatId, "Неизвестная команда 🤷‍♂️");
            messageService.sendMenu(bot, chatId, telegramId);
        }
    }

    // 🔥 ВЫНЕСЕННАЯ ЛОГИКА LIVE
    private void handleLiveMatch(Long chatId, TelegramLongPollingBot bot) throws Exception {

        String link = liveMatchService.getLink(chatId);

        if (link == null) {
            liveMatchService.startWaiting(chatId);
            messageService.send(bot, chatId, "Скинь ссылку на турнир");
            return;
        }

        Document doc = Jsoup.connect(link).get();

        Match live = matchParser.findLiveMatch(doc);

        // 🔥 1. ЕСЛИ МАТЧ ИДЕТ → ПОКАЗЫВАЕМ
        if (live != null) {
            messageService.send(bot, chatId,
                    "🔥 LIVE\n\n" +
                            live.getPlayer1() + "\n" +
                            live.getScore1() + ":" + live.getScore2() + " " + live.getSetsDetails() + "\n" +
                            live.getPlayer2()
            );
            return;
        }

        // 🔥 2. ЕСЛИ ТУРНИР ЗАВЕРШЕН → СБРАСЫВАЕМ
        if (matchParser.isTournamentFinished(doc)) {
            liveMatchService.clear(chatId);
            messageService.send(bot, chatId, "🏁 Турнир завершен");
            return;
        }

        // 🔥 3. НЕТ ЛАЙВА → ПРОСТО ЖДЕМ (ВОТ ЭТО ТЕБЕ НЕ ХВАТАЛО)
        messageService.send(bot, chatId, "⏳ Сейчас нет активного матча...");
    }
}