package com.example.parser.bot.handler;

import com.example.parser.config.AdminProperties;
import com.example.parser.formatter.LiveMatchFormatter;
import com.example.parser.formatter.LiveMatchImageRenderer;
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
import org.telegram.telegrambots.meta.api.objects.Message;
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
    private final LiveMatchFormatter liveMatchFormatter;

    // 🔥 ДОБАВИЛИ
    private final LiveMatchImageRenderer imageRenderer;

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

            if (data.equals("reset_live")) {
                liveMatchService.clear(chatId);
                liveMatchService.clearMessageId(chatId);
                liveMatchService.stopAutoUpdate(chatId);
                liveMatchService.stopWaiting(chatId);
                liveMatchService.clearLastMessage(chatId);

                messageService.send(bot, chatId, "🚪 Вы вышли из лайва");
                return;
            }

            if (data.equals("live_match")) {
                handleLiveMatch(chatId, bot);
                return;
            }

            if (data.startsWith("date_") || data.startsWith("month_") || data.equals("ignore")) {
                adminHandler.handleCalendarCallback(chatId, data, bot);
                return;
            }

            if (data.startsWith("player_")) {
                Long playerId = Long.parseLong(data.replace("player_", ""));
                adminHandler.handlePlayerSelected(chatId, playerId, bot);
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

        if (liveMatchService.isWaiting(chatId) && text.startsWith("http")) {
            liveMatchService.setLink(chatId, text);
            messageService.send(bot, chatId, "🔥 Трансляция запущена\nСчет обновляется автоматически");
            handleLiveMatch(chatId, bot);
            return;
        }

        if (text.equals("🔥 Лайв матч")) {
            liveMatchService.startWaiting(chatId);
            messageService.send(bot, chatId, "Скинь ссылку на турнир");
            return;
        }

        // остальное не трогаем 👇
        if (text.equals("/start")) {
            if (player == null) {
                startHandler.handle(update, bot);
            } else {
                messageService.send(bot, chatId, "С возвращением, " + player.getName());
                messageService.sendMenu(bot, chatId, telegramId);
            }
            return;
        }
    }

    private void handleLiveMatch(Long chatId, TelegramLongPollingBot bot) throws Exception {

        String link = liveMatchService.getLink(chatId);

        if (link == null) {
            if (!liveMatchService.isWaiting(chatId)) {
                return;
            }
            messageService.send(bot, chatId, "Скинь ссылку на турнир");
            return;
        }

        if (!liveMatchService.isAutoUpdating(chatId)) {
            liveMatchService.startAutoUpdate(chatId);

            new Thread(() -> {
                while (liveMatchService.isAutoUpdating(chatId)) {
                    try {
                        Thread.sleep(5000);
                        handleLiveMatch(chatId, bot);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        Document doc = Jsoup.connect(link).get();
        Match live = matchParser.findLiveMatch(doc);

        // 🔥 ВОТ ГЛАВНЫЙ ФИКС
        if (live != null) {

            String hash = live.toString();
            if (!shouldUpdate(chatId, hash)) {
                return;
            }

            byte[] image = imageRenderer.render(
                    live.getPlayer1(),
                    live.getScore1(),
                    live.getSetsDetails(),
                    live.getPlayer2(),
                    live.getScore2(),
                    live.getSetsDetails(),
                    live.getLeague(),
                    live.getTable()
            );

            messageService.sendImage(bot, chatId, image);
            return;
        }

        if (matchParser.isTournamentFinished(doc)) {
            liveMatchService.clear(chatId);
            liveMatchService.clearMessageId(chatId);
            liveMatchService.stopAutoUpdate(chatId);
            liveMatchService.clearLastMessage(chatId);

            messageService.send(bot, chatId, "🏁 Турнир завершен");
            return;
        }

        String text = "⏳ Сейчас нет активного матча...";
        if (!shouldUpdate(chatId, text)) return;

        messageService.send(bot, chatId, text);
    }

    private boolean shouldUpdate(Long chatId, String newText) {
        String last = liveMatchService.getLastMessage(chatId);
        if (newText.equals(last)) return false;

        liveMatchService.setLastMessage(chatId, newText);
        return true;
    }
}