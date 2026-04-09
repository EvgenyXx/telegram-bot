package com.example.parser.bot.handler;

import com.example.parser.bot.CallBackInline.CollBackRouter;
import com.example.parser.notification.MessageService;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
public class CallbackHandler {

    private final PlayerService playerService;
    private final MessageService messageService;
    private final CollBackRouter collBackRouter;

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        if (update.getCallbackQuery() == null) {
            log.warn("Received update without callbackQuery");
            return;
        }

        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long telegramId = update.getCallbackQuery().getFrom().getId();
        String data = update.getCallbackQuery().getData();

        bot.execute(new AnswerCallbackQuery(update.getCallbackQuery().getId()));

        log.debug("Callback received: chatId={}, data={}", chatId, data);

        Player player = playerService.getByTelegramId(telegramId);

        if (isBlocked(player, chatId, bot)) return;

        try {
            collBackRouter.route(update, bot);
            log.debug("Callback processed: chatId={}, data={}", chatId, data);
        } catch (Exception e) {
            log.error("Error while processing callback: chatId={}, data={}", chatId, data, e);
            throw e;
        }
    }

    private boolean isBlocked(Player player, Long chatId, TelegramLongPollingBot bot) {
        if (player != null && player.isBlocked()) {
            messageService.send(bot, chatId, "🚫 Ты заблокирован");
            return true;
        }
        return false;
    }
}