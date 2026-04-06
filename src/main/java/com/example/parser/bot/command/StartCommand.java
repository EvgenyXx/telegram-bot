package com.example.parser.bot.command;

import com.example.parser.notification.MessageService;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Order(1)
public class StartCommand implements CommandHandler {

    private final MessageService messageService;
    private final PlayerService playerService;

    @Override
    public boolean supports(String text, Player player) {
        return "/start".equals(text);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) {


        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        Player player = playerService.getByTelegramId(telegramId);
        System.out.println("PLAYER FROM DB: " + player);

        if (player == null) {
            messageService.send(bot, chatId,
                    getWelcomeMessage()
            );
            return;
        }

        if (player.getName() == null) {
            messageService.send(bot, chatId,
                    "❗ Введи: Фамилия Имя\nПример: Иванов Иван");
            return;
        }

        messageService.send(bot, chatId,
                "С возвращением, " + player.getName());

        messageService.sendMenu(bot, chatId, telegramId, null);
    }



    private String getWelcomeMessage() {
        return "👋 Добро пожаловать!\n\n" +
                "❗ Введи данные в формате:\n" +
                "Фамилия Имя\n\n" +
                "⚠️ Сначала фамилия, потом имя\n" +
                "Иначе бот не сможет тебя найти\n\n" +
                "Пример: Иванов Иван";
    }
}