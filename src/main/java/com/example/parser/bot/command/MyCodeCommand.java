package com.example.parser.bot.command;

import com.example.parser.bot.handler.CommandHandler;
import com.example.parser.modules.notification.service.MessageService;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.service.PlayerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class MyCodeCommand implements CommandHandler {

    private static final String MY_CODE = "/mycode";
    private final PlayerService playerService;
    private final MessageService messageService;

    @Override
    public boolean supports(String text, Player player) {
        return MY_CODE.equals(text) && player != null;
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        Player player = playerService.getByTelegramId(telegramId);

        if (player.getAccessCode() == null) {
            player = playerService.generateAccessCode(telegramId);//todo доделать
        }

        messageService.send(bot, chatId,
                "🔑 Ваш код доступа: <b>" + player.getAccessCode() + "</b>\n\n" +
                        "Используйте его на сайте: http://protennis-store.ru/player");
    }
}