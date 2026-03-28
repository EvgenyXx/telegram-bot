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

    public void handle(Update update, TelegramLongPollingBot bot) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        // 👉 старт
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

            messageService.sendInlineKeyboard(
                    bot,
                    chatId,
                    "Выбери игрока 👇",
                    keyboard
            );
        }
    }

    public boolean isInProgress(Long chatId) {
        return userState.containsKey(chatId);
    }

    // 👉 выбор игрока
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

        messageService.sendInlineKeyboard(
                bot,
                chatId,
                "Выбери действие 👇",
                keyboard
        );
    }

    // 👉 открыть календарь
    public void openCalendar(Long chatId, TelegramLongPollingBot bot) {

        YearMonth now = YearMonth.now();
        currentMonthMap.put(chatId, now);

        messageService.sendInlineKeyboard(
                bot,
                chatId,
                "Выбери дату начала 👇",
                CalendarKeyboardBuilder.build(now)
        );
    }

    // 👉 обработка календаря
    public void handleCalendarCallback(Long chatId, String data, TelegramLongPollingBot bot) {

        if (data.equals("ignore")) return;

        if (data.startsWith("month_")) {

            YearMonth month = YearMonth.parse(data.replace("month_", ""));
            currentMonthMap.put(chatId, month);

            messageService.sendInlineKeyboard(
                    bot,
                    chatId,
                    "Выбери дату 👇",
                    CalendarKeyboardBuilder.build(month)
            );
            return;
        }

        if (data.startsWith("date_")) {

            LocalDate date = LocalDate.parse(data.replace("date_", ""));

            // первая дата
            if (!startDateMap.containsKey(chatId)) {
                startDateMap.put(chatId, date);
                messageService.send(bot, chatId, "📅 Теперь выбери конечную дату");
                return;
            }

            // вторая дата
            LocalDate start = startDateMap.get(chatId);
            LocalDate end = date;

            startDateMap.remove(chatId);

            var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            String formatted =
                    start.format(formatter) + " " +
                            end.format(formatter);

            fakeDateInput(chatId, bot, formatted);
        }
    }

    // 👉 обработка периода
    private void fakeDateInput(Long chatId, TelegramLongPollingBot bot, String text) {

        String state = userState.get(chatId);
        Player player = selectedPlayer.get(chatId);

        if (player == null || state == null) {
            messageService.send(bot, chatId, "❌ Ошибка состояния");
            return;
        }

        String[] parts = text.split(" ");
        var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        LocalDate start = LocalDate.parse(parts[0], formatter);
        LocalDate end = LocalDate.parse(parts[1], formatter);

        if ("PLAYER_TOURNAMENTS".equals(state)) {

            var results = tournamentResultService.getResultsByPeriod(player, start, end);

            StringBuilder sb = new StringBuilder("📅 Турниры игрока:\n\n");

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

        if ("PLAYER_SUM".equals(state)) {

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
}