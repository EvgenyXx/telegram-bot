package com.example.parser.bot.command;

import com.example.parser.bot.handler.AdminHandler;
import com.example.parser.modules.notification.service.MessageService;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.service.PlayerService;
import com.example.parser.modules.shared.exception.BotExceptionHandler;
import com.example.parser.modules.tournament.calendar.domain.CalendarSession;
import com.example.parser.modules.tournament.calendar.service.CalendarSessionService;
import com.example.parser.modules.tournament.calendar.domain.CalendarState;
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
    private final AdminHandler adminHandler;
    private final CalendarSessionService sessionService;
    private final BotExceptionHandler exceptionHandler;

    public void handle(Update update, TelegramLongPollingBot bot) {

        Long chatId = update.getMessage().getChatId();

        try {
            String text = update.getMessage().getText();
            Long telegramId = update.getMessage().getFrom().getId();

            Player player = playerService.getByTelegramId(telegramId);

            // 🚫 блок
            if (player != null && player.isBlocked()) {
                messageService.send(bot, chatId, "🚫 Ты заблокирован");
                return;
            }

            // 🔥 STATE ПЕРЕХВАТ
            CalendarSession session = sessionService.get(chatId);
            if (session != null && session.getState() == CalendarState.SEARCH_PLAYER) {
                adminHandler.search(chatId, text, bot);
                sessionService.remove(chatId);
                return;
            }

            // 👇 обычные команды
            for (CommandHandler handler : handlers) {
                if (handler.supports(text, player)) {
                    handler.handle(update, bot); // 💥 тут может упасть
                    return;
                }
            }

            // fallback
            messageService.send(bot, chatId, "Неизвестная команда 🤷‍♂️");

        } catch (Exception e) {
            exceptionHandler.handle(e, bot, chatId); // ✅ ВОТ ГДЕ ВСЯ МАГИЯ
        }
    }
}