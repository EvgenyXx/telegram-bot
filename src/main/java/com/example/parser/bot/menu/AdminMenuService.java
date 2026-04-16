package com.example.parser.bot.menu;

import com.example.parser.modules.notification.service.MessageService;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.service.PlayerService;
import com.example.parser.modules.shared.AdminProperties;
import com.example.parser.modules.tournament.calendar.domain.CalendarSession;
import com.example.parser.modules.tournament.calendar.service.CalendarSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMenuService {

    private final PlayerService playerService;
    private final MessageService messageService;
    private final AdminProperties adminProperties;
    private final CalendarSessionService sessionService;
    private final InlineKeyboardBuilder kb;

    public void showPlayers(Long chatId, TelegramLongPollingBot bot) throws Exception {

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Player p : playerService.getAll()) {
            rows.add(kb.row(
                    kb.button(p.getName(), "player_" + p.getId())
            ));
        }

        messageService.sendInlineKeyboard(
                bot,
                chatId,
                "Выбери игрока 👇",
                kb.keyboard(rows)
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

        CalendarSession session = sessionService.get(chatId);
        session.setPlayerId(player.getId());

        Long adminTelegramId = session.getTelegramId(); // 🔥 берем отсюда

        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        firstRow.add(kb.button("📅 Турниры", "tournaments"));
        firstRow.add(kb.button("💰 Сумма", "sum"));

        // 🔥 КНОПКА КОРРЕКТИРОВКИ
        if (adminProperties.isSuperAdmin(adminTelegramId)) {
            firstRow.add(kb.button("✏️ Корректировка", "adjust_sum_" + playerId));
        }

        List<List<InlineKeyboardButton>> rows = List.of(
                firstRow,
                kb.row(buildActionButton(player))
        );

        messageService.sendInlineKeyboard(
                bot,
                chatId,
                "Выбери действие 👇",
                kb.keyboard(rows)
        );
    }

    private InlineKeyboardButton buildActionButton(Player player) {

        if (adminProperties.isAdmin(player.getTelegramId())) {
            return kb.button("👑 Администратор", "ignore");
        }

        if (player.isBlocked()) {
            return kb.button("✅ Разблокировать", "unblock_user_" + player.getId());
        }

        return kb.button("🚫 Заблокировать", "block_user_" + player.getId());
    }

    public void searchWithPagination(Long chatId, String query, int page, TelegramLongPollingBot bot) throws Exception {

        Page<Player> result = playerService.search(query, page, 5);

        if (result.isEmpty()) {
            messageService.send(bot, chatId, "❌ Ничего не найдено");
            return;
        }

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Player p : result.getContent()) {
            rows.add(kb.row(
                    kb.button(p.getName(), "player_" + p.getId())
            ));
        }

        List<InlineKeyboardButton> nav = new ArrayList<>();

        if (page > 0) {
            nav.add(kb.button("⬅️", "search|" + query + "|" + (page - 1)));
        }

        if (result.hasNext()) {
            nav.add(kb.button("➡️", "search|" + query + "|" + (page + 1)));
        }

        if (!nav.isEmpty()) {
            rows.add(nav);
        }

        messageService.sendInlineKeyboard(
                bot,
                chatId,
                "🔍 Найдено: " + result.getTotalElements(),
                kb.keyboard(rows)
        );
    }
}