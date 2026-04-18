package com.example.parser.bot.command;

import com.example.parser.bot.handler.CommandHandler;
import com.example.parser.modules.notification.service.MessageService;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.service.PlayerService;
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
                "Фамилия Имя\n" +
                "Пример: Иванов Иван\n\n" +
                "🏓 После регистрации бот начнёт отслеживать твои турниры,\n" +
                "считать результаты и вести личную статистику\n\n" +
                "📊 В статистику попадают турниры, начиная с момента регистрации,\n" +
                "а также те, которые ты добавишь сам, отправив ссылку или через «Поделиться»\n\n" +
                "🚀 Хочешь добавить прошлые турниры — просто отправь ссылки\n" +
                "и они появятся в разделе «Мои турниры»";
    }
}