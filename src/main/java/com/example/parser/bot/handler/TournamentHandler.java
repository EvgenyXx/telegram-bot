package com.example.parser.bot.handler;

import com.example.parser.config.AdminProperties;
import com.example.parser.domain.dto.ResultDto;
import com.example.parser.notification.MessageService;
import com.example.parser.notification.formatter.TournamentMessageFormatter;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import com.example.parser.tournament.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TournamentHandler {

    private final ResultService resultService;
    private final PlayerService playerService;
    private final MessageService messageService;
    private final TournamentMessageFormatter formatter;


    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        Player player = playerService.getByTelegramId(telegramId);



        if (player == null) {
            messageService.send(bot, chatId, "❌ Ты не зарегистрирован");
            return;
        }

        if (player.isBlocked()) {
            messageService.send(bot, chatId, "🚫 Ты заблокирован");
            return;
        }

        // 🔥 парсим турнир
        ResultService.ParsedResult parsed = resultService.calculateAll(text);
        List<ResultDto> results = parsed.getResults();
        double bonus = parsed.getNightBonus();

        // 🔥 формируем сообщение
        String message = formatter.formatFinalWithPlayer(
                results,
                bonus,
                player.getName()
        );

        // 📩 отправка
        messageService.send(bot, chatId, message);

        // 📋 меню
        messageService.sendMenu(bot, chatId, telegramId, null);
    }
}