package com.example.parser.bot;

import com.example.parser.config.AdminProperties;
import com.example.parser.notification.MessageService;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import com.example.parser.tournament.calendar.CalendarService;
import com.example.parser.tournament.calendar.CalendarSession;
import com.example.parser.tournament.calendar.CalendarSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMenuService {

    private final PlayerService playerService;
    private final MessageService messageService;
    private final CalendarService calendarService;
    private final AdminProperties adminProperties;
    private final CalendarSessionService sessionService;

    public void showPlayers(Long chatId, TelegramLongPollingBot bot) throws Exception {

        List<Player> players = playerService.getAll();

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Player p : players) {
            rows.add(List.of(
                    button(p.getName(), "player_" + p.getId())
            ));
        }

        keyboard.setKeyboard(rows);

        messageService.sendInlineKeyboard(
                bot,
                chatId,
                "Выбери игрока 👇",
                keyboard
        );
    }

    public void handlePlayerSelected(Long chatId,
                                     Long playerId,
                                     TelegramLongPollingBot bot) throws Exception {

        Player player = playerService.findById(playerId);

        if (player == null) {
            messageService.send(bot, chatId, "❌ Игрок не найден");
            return;
        }

//        calendarService.setPlayer(chatId, player);

        CalendarSession session = sessionService.get(chatId);
        session.setPlayerId(player.getId());

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

        keyboard.setKeyboard(List.of(
                List.of(
                        button("📅 Турниры", "tournaments"),
                        button("💰 Сумма", "sum")
                ),
                List.of(
                        buildActionButton(player)
                )
        ));

        messageService.sendInlineKeyboard(
                bot,
                chatId,
                "Выбери действие 👇",
                keyboard
        );
    }

    // ================== HELPERS ==================

    private InlineKeyboardButton button(String text, String callback) {
        InlineKeyboardButton btn = new InlineKeyboardButton();
        btn.setText(text);
        btn.setCallbackData(callback);
        return btn;
    }

    private InlineKeyboardButton buildActionButton(Player player) {

        if (adminProperties.isAdmin(player.getTelegramId())) {
            return button("👑 Администратор", "ignore");
        }

        if (player.isBlocked()) {
            return button("✅ Разблокировать",
                    "unblock_user_" + player.getId());
        }

        return button("🚫 Заблокировать",
                "block_user_" + player.getId());
    }


    public void searchWithPagination(Long chatId, String query, int page, TelegramLongPollingBot bot) throws Exception {

        int size = 5;

        Page<Player> result = playerService.search(query, page, size);

        if (result.isEmpty()) {
            messageService.send(bot, chatId, "❌ Ничего не найдено");
            return;
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Player p : result.getContent()) {
            rows.add(List.of(button(p.getName(), "player_" + p.getId())));
        }

        // 🔥 НАВИГАЦИЯ
        List<InlineKeyboardButton> nav = new ArrayList<>();

        if (page > 0) {
            nav.add(button("⬅️", "search|" + query + "|" + (page - 1)));
        }

        if (result.hasNext()) {
            nav.add(button("➡️", "search|" + query + "|" + (page + 1)));
        }

        if (!nav.isEmpty()) {
            rows.add(nav);
        }

        keyboard.setKeyboard(rows);

        messageService.sendInlineKeyboard(
                bot,
                chatId,
                "🔍 Найдено: " + result.getTotalElements(),
                keyboard
        );
    }
}