package com.example.parser.bot.command;

import com.example.parser.bot.handler.AdminHandler;
import com.example.parser.notification.MessageService;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import com.example.parser.tournament.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CommandRouter {

    private final List<CommandHandler> handlers;
    private final PlayerService playerService;
    private final MessageService messageService;
    private final CalendarService calendarService;
    private final AdminHandler adminHandler;

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        Player player = playerService.getByTelegramId(telegramId);

        // 🚫 блок
        if (player != null && player.isBlocked()) {
            messageService.send(bot, chatId, "🚫 Ты заблокирован");
            return;
        }

        // 🔥 STATE ПЕРЕХВАТ (ПОИСК)
        String state = calendarService.getState(chatId);

        if ("SEARCH_PLAYER".equals(state)) {
            adminHandler.search(chatId, text, bot);
            return;
        }

        // 👇 обычные команды
        for (CommandHandler handler : handlers) {
            if (handler.supports(text, player)) {
                handler.handle(update, bot);
                return;
            }
        }

        // fallback
        messageService.send(bot, chatId, "Неизвестная команда 🤷‍♂️");
    }
}