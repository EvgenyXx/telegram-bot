package com.example.parser.bot.handler;

import com.example.parser.notification.MessageService;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import com.example.parser.tournament.TournamentResultService;
import com.example.parser.tournament.calendar.CalendarSession;
import com.example.parser.tournament.calendar.CalendarSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@Component
@RequiredArgsConstructor
public class TextHandler {

    private final CalendarSessionService sessionService;
    private final AdminHandler adminHandler;
    private final FixSessionService fixSessionService;
    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;
    private final MessageService messageService;

    public boolean handle(Long chatId, String text, TelegramLongPollingBot bot) throws Exception {

        FixSession fixSession = fixSessionService.get(chatId);

        try {
            double amount = Double.parseDouble(text.trim());

            Player player = playerService.findById(fixSession.getPlayerId());

            tournamentResultService.updateAmount(
                    player,
                    fixSession.getTournamentId(),
                    amount
            );

            messageService.send(bot, chatId, "✅ Обновлено");
            fixSessionService.remove(chatId);

        } catch (Exception e) {
            messageService.send(bot, chatId, "❌ Введите число");
        }

        CalendarSession session = sessionService.get(chatId);


        return false;
    }
}