package com.example.parser.bot.command;

import com.example.parser.bot.handler.CommandHandler;
import com.example.parser.core.dto.ResultDto;
import com.example.parser.modules.notification.service.MessageService;
import com.example.parser.modules.notification.formatter.TournamentMessageFormatter;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.service.PlayerService;
import com.example.parser.modules.tournament.domain.ParsedResult;
import com.example.parser.modules.tournament.application.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
@Order(20)
public class TournamentParseCommand implements CommandHandler {

    private final ResultService resultService;
    private final PlayerService playerService;
    private final MessageService messageService;
    private final TournamentMessageFormatter formatter;

    @Override
    public boolean supports(String text, Player player) {
        // ⚠️ временно так (потом можно улучшить)
        return text.length() > 20;
    }

    @Override
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

        ParsedResult parsed = resultService.calculateAll(text);
        List<ResultDto> results = parsed.getResults();



        String message = formatter.format(
                results
        );

        messageService.send(bot, chatId, message);
        messageService.sendMenu(bot, chatId, telegramId, null);
    }
}