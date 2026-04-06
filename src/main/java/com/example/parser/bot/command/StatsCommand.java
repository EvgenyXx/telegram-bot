package com.example.parser.bot.command;

import com.example.parser.domain.dto.FullStatsDto;
import com.example.parser.notification.MessageService;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import com.example.parser.stats.StatsFormatter;
import com.example.parser.tournament.TournamentResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Order(3)
public class StatsCommand implements CommandHandler {

    private final TournamentResultService tournamentResultService;
    private final StatsFormatter statsFormatter;
    private final MessageService messageService;
    private final PlayerService playerService;

    @Override
    public boolean supports(String text, Player player) {
        return "📊 Моя статистика".equals(text);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) {

        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        Player player = playerService.getByTelegramId(telegramId);

        FullStatsDto stats = tournamentResultService.getFullStats(player);

        if (stats == null) {
            messageService.send(bot, chatId, "📊 Пока нет статистики");
            return;
        }

        messageService.send(bot, chatId, statsFormatter.formatFullStats(stats));
    }
}