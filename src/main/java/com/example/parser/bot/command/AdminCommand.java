package com.example.parser.bot.command;


import com.example.parser.bot.handler.CommandHandler;
import com.example.parser.modules.notification.service.MessageService;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.shared.AdminProperties;
import com.example.parser.modules.tournament.calendar.service.CalendarService;
import com.example.parser.modules.tournament.calendar.domain.CalendarState;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Order(3)
public class AdminCommand implements CommandHandler {

    private final MessageService messageService;
    private final AdminProperties adminProperties;
    private final CalendarService calendarService;

    @Override
    public boolean supports(String text, Player player) {
        return "📊 Статистика".equals(text)
                && player != null
                && adminProperties.isAdmin(player.getTelegramId());
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) {
        Long chatId = update.getMessage().getChatId();

        // 🔥 ВКЛЮЧАЕМ ПОИСК
        calendarService.setState(chatId, CalendarState.SEARCH_PLAYER);
        messageService.send(bot, chatId, "🔍 Введи фамилию или имя");
    }
}