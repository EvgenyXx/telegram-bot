package com.example.parser.bot.handler;

import com.example.parser.dto.PeriodStatsProjection;
import com.example.parser.entity.Player;
import com.example.parser.service.MessageService;
import com.example.parser.service.PlayerService;
import com.example.parser.service.TournamentResultService;
import com.example.parser.util.CalendarKeyboardBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public class AdminHandler {

    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;
    private final MessageService messageService;

    public final Map<Long, String> userState = new HashMap<>();
    private final Map<Long, Player> selectedPlayer = new HashMap<>();
    private final Map<Long, LocalDate> startDateMap = new HashMap<>();
    private final Map<Long, YearMonth> currentMonthMap = new HashMap<>();
    private final Map<Long, Integer> calendarMessageId = new HashMap<>();

    // 👉 старт (админка)
    public void handle(Update update, TelegramLongPollingBot bot) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        if (text.equals("📊 Статистика")) {
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
    }

    public boolean isInProgress(Long chatId) {
        return userState.containsKey(chatId);
    }

    // 👉 выбор игрока (админ)
    public void handlePlayerSelected(Long chatId, Long playerId, TelegramLongPollingBot bot) {
        Player player = playerService.findById(playerId);

        if (player == null) {
            messageService.send(bot, chatId, "❌ Игрок не найден");
            return;
        }

        selectedPlayer.put(chatId, player);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();

        InlineKeyboardButton btn1 = new InlineKeyboardButton();
        btn1.setText("📅 Турниры");
        btn1.setCallbackData("tournaments");

        InlineKeyboardButton btn2 = new InlineKeyboardButton();
        btn2.setText("💰 Сумма");
        btn2.setCallbackData("sum");

        keyboard.setKeyboard(List.of(List.of(btn1, btn2)));

        messageService.sendInlineKeyboard(bot, chatId, "Выбери действие 👇", keyboard);
    }

    // 👉 открыть календарь
    public void openCalendar(Long chatId, TelegramLongPollingBot bot) {
        try {
            YearMonth now = YearMonth.now();
            currentMonthMap.put(chatId, now);

            var msg = messageService.sendInlineKeyboardAndGetMessage(
                    bot,
                    chatId,
                    "Выбери дату начала 👇",
                    CalendarKeyboardBuilder.build(now, null, null)
            );

            calendarMessageId.put(chatId, msg.getMessageId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 👉 обработка календаря
    public void handleCalendarCallback(Long chatId, String data, TelegramLongPollingBot bot) {

        if (data.equals("ignore")) return;

        YearMonth currentMonth = currentMonthMap.get(chatId);
        LocalDate start = startDateMap.get(chatId);

        // 👉 смена месяца
        if (data.startsWith("month_")) {
            YearMonth month = YearMonth.parse(data.replace("month_", ""));
            currentMonthMap.put(chatId, month);

            updateCalendar(chatId, bot, "Выбери дату 👇", month, start, null);
            return;
        }

        // 👉 выбор даты
        if (data.startsWith("date_")) {
            LocalDate date = LocalDate.parse(data.replace("date_", ""));

            // первая дата
            if (start == null) {
                startDateMap.put(chatId, date);

                updateCalendar(
                        chatId,
                        bot,
                        "Начало: " + date + "\nВыбери конец 👇",
                        currentMonth,
                        date,
                        null
                );
                return;
            }

            // вторая дата
            LocalDate end = date;

            if (end.isBefore(start)) {
                LocalDate tmp = start;
                start = end;
                end = tmp;
            }

            updateCalendar(chatId, bot, "Готово 👇", currentMonth, start, end);

            startDateMap.remove(chatId);

            var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            String formatted =
                    start.format(formatter) + " " +
                            end.format(formatter);

            fakeDateInput(chatId, bot, formatted);
        }
    }

    // 👉 ГЛАВНЫЙ ФИКС ТУТ
    private void fakeDateInput(Long chatId, TelegramLongPollingBot bot, String text) {

        String state = userState.get(chatId);

        Player player = selectedPlayer.get(chatId);

        // ✅ ФИКС: теперь берём по telegramId
        if (player == null) {
            try {
                player = playerService.getByTelegramId(chatId);
            } catch (Exception e) {
                player = null;
            }
        }

        if (player == null || state == null) {
            messageService.send(bot, chatId, "❌ Ошибка состояния");
            return;
        }

        String[] parts = text.split(" ");
        var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        LocalDate start = LocalDate.parse(parts[0], formatter);
        LocalDate end = LocalDate.parse(parts[1], formatter);

        // турниры
        if ("PLAYER_TOURNAMENTS".equals(state) || "USER_TOURNAMENTS".equals(state)) {

            var results = tournamentResultService.getResultsByPeriod(player, start, end);

            StringBuilder sb = new StringBuilder("📅 Турниры:\n\n");

            if (results.isEmpty()) {
                sb.append("❌ Ничего не найдено");
            } else {
                results.forEach(r ->
                        sb.append(r.getDate())
                                .append(" — ")
                                .append(r.getAmount())
                                .append("\n")
                );
            }

            messageService.send(bot, chatId, sb.toString());
        }

        // сумма
        if ("PLAYER_SUM".equals(state) || "USER_SUM".equals(state)) {

            PeriodStatsProjection stats =
                    tournamentResultService.getStatsByPeriod(player, start, end);

            String response =
                    "💰 Сумма: " + stats.getSum() +
                            "\n📊 Среднее: " + stats.getAverage() +
                            "\n💸 Сумма -3%: " + stats.getMinusThreePercent();

            messageService.send(bot, chatId, response);
        }

        userState.remove(chatId);
        selectedPlayer.remove(chatId);

        messageService.sendMenu(bot, chatId);
    }

    private void updateCalendar(Long chatId,
                                TelegramLongPollingBot bot,
                                String text,
                                YearMonth month,
                                LocalDate start,
                                LocalDate end) {
        try {
            Integer messageId = calendarMessageId.get(chatId);
            if (messageId == null) return;

            var edit = new org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText();
            edit.setChatId(chatId.toString());
            edit.setMessageId(messageId);
            edit.setText(text);
            edit.setReplyMarkup(CalendarKeyboardBuilder.build(month, start, end));

            bot.execute(edit);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}