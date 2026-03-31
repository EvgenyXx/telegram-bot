package com.example.parser.bot.handler;

import com.example.parser.config.AdminProperties;
import com.example.parser.domain.entity.Player;
import com.example.parser.service.MessageService;
import com.example.parser.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class CallbackHandler {

    private final AdminHandler adminHandler;
    private final LiveMatchHandler liveMatchHandler;
    private final PlayerService playerService;
    private final MessageService messageService;
    private final AdminProperties adminProperties;

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long telegramId = update.getCallbackQuery().getFrom().getId();
        String data = update.getCallbackQuery().getData();

        bot.execute(new AnswerCallbackQuery(update.getCallbackQuery().getId()));

        Player player = playerService.getByTelegramId(telegramId);
        if (player != null && player.isBlocked()) {
            messageService.send(bot, chatId, "🚫 Ты заблокирован");
            return;
        }

        if (data.equals("reset_live")) {
            liveMatchHandler.stop(chatId, bot);
            return;
        }

        if (data.startsWith("date_") || data.startsWith("month_") || data.equals("ignore")) {
            adminHandler.handleCalendarCallback(chatId, data, bot);
            return;
        }

        if (data.startsWith("player_")) {
            adminHandler.handlePlayerSelected(chatId,
                    Long.parseLong(data.replace("player_", "")), bot);
            return;
        }

        if (data.equals("live_match")) {
            liveMatchHandler.start(chatId, bot);
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
        }

        if (data.equals("info")) {
            liveMatchHandler.sendInfo(chatId, bot);
            return;
        }
    }
}