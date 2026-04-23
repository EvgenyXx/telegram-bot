package com.example.parser.bot.handler;

import com.example.parser.bot.ui.menu.AdminMenuService;
import com.example.parser.modules.notification.service.MessageService;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.service.PlayerService;
import com.example.parser.modules.tournament.application.TournamentResultService;
import com.example.parser.modules.tournament.calendar.service.CalendarService;
import com.example.parser.modules.tournament.calendar.domain.CalendarState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

@Component
@RequiredArgsConstructor
public class AdminHandler  {

    private final AdminMenuService adminMenuService;
    private final CalendarService calendarService;
    private final TournamentResultService resultService;
    private final MessageService messageService;
    private final PlayerService playerService;


    public void handlePlayerSelected(Long chatId, Long playerId, TelegramLongPollingBot bot) throws Exception {

        adminMenuService.handlePlayerSelected(chatId, playerId, bot);
    }

    public void updateSum(Long chatId,
                          Long tournamentId,
                          Long playerId,
                          Long sum,
                          TelegramLongPollingBot bot) throws Exception {

        Player player = playerService.findById(playerId); // 👈 ВОТ ЭТО ДОБАВИТЬ

        resultService.updateAmount(
                player,
                tournamentId,
                sum.doubleValue()
        );

        messageService.send(bot, chatId, "✅ Сумма обновлена");
    }
    public void openCalendar(Long chatId, Long telegramId, CalendarState state, TelegramLongPollingBot bot) {

        calendarService.setState(chatId, state);
        calendarService.open(chatId, telegramId, bot);
    }

    public void handleCalendarCallback(Long chatId, String data, TelegramLongPollingBot bot) {
        calendarService.handleCallback(chatId, data, bot);
    }


    // 🔥 ПОИСК
    public void search(Long chatId, String query, TelegramLongPollingBot bot) throws Exception {
        adminMenuService.searchWithPagination(chatId, query, 0, bot);
    }

    public void searchPage(Long chatId, String query, int page, TelegramLongPollingBot bot) throws Exception {
        adminMenuService.searchWithPagination(chatId, query, page, bot);
    }
}