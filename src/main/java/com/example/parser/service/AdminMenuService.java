package com.example.parser.service;

import com.example.parser.entity.Player;
import lombok.RequiredArgsConstructor;
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
    private final CalendarService calendarService; // 👈 ДОБАВИТЬ

    public void showPlayers(Long chatId, TelegramLongPollingBot bot) {
        List<Player> players = playerService.getAll();

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Player p : players) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(p.getName());
            btn.setCallbackData("player_" + p.getId());
            rows.add(List.of(btn));
        }

        keyboard.setKeyboard(rows);

        messageService.sendInlineKeyboard(bot, chatId, "Выбери игрока 👇", keyboard);
    }

    public void handlePlayerSelected(Long chatId, Long playerId, TelegramLongPollingBot bot) {
        Player player = playerService.findById(playerId);

        if (player == null) {
            messageService.send(bot, chatId, "❌ Игрок не найден");
            return;
        }

        // 🔥 ВАЖНО — сохраняем игрока в календарь
        calendarService.setPlayer(chatId, player);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

        InlineKeyboardButton btn1 = new InlineKeyboardButton();
        btn1.setText("📅 Турниры");
        btn1.setCallbackData("tournaments");

        InlineKeyboardButton btn2 = new InlineKeyboardButton();
        btn2.setText("💰 Сумма");
        btn2.setCallbackData("sum");

// 🔥 динамическая кнопка
        InlineKeyboardButton actionBtn = new InlineKeyboardButton();

        if (player.isBlocked()) {
            actionBtn.setText("✅ Разблокировать");
            actionBtn.setCallbackData("unblock_user_" + player.getId());
        } else {
            actionBtn.setText("🚫 Заблокировать");
            actionBtn.setCallbackData("block_user_" + player.getId());
        }

        keyboard.setKeyboard(List.of(
                List.of(btn1, btn2),
                List.of(actionBtn)
        ));

        messageService.sendInlineKeyboard(bot, chatId, "Выбери действие 👇", keyboard);
    }



}