package com.example.parser.service;

import com.example.parser.entity.Player;
import com.example.parser.util.CalendarKeyboardBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final PlayerService playerService;
    private final TournamentResultService tournamentResultService;
    private final MessageService messageService;

    private final Map<Long, String> userState = new HashMap<>();
    private final Map<Long, Long> selectedPlayerId = new HashMap<>();
    private final Map<Long, LocalDate> startDateMap = new HashMap<>();
    private final Map<Long, YearMonth> currentMonthMap = new HashMap<>();
    private final Map<Long, Integer> calendarMessageId = new HashMap<>();
    private final Map<Long, LocalDate> endDateMap = new HashMap<>();

    // 🔥 telegramId
    private final Map<Long, Long> telegramIdMap = new HashMap<>();

    public void setState(Long chatId, String state) {
        userState.put(chatId, state);
    }

    public void setPlayer(Long chatId, Player player) {
        if (player != null) {
            selectedPlayerId.put(chatId, player.getId());
        }
    }

    public boolean isInProgress(Long chatId) {
        return userState.containsKey(chatId);
    }

    // 🔥 ОТКРЫТИЕ КАЛЕНДАРЯ
    public void open(Long chatId, Long telegramId, TelegramLongPollingBot bot) {
        try {
            telegramIdMap.put(chatId, telegramId);

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

    public void handleCallback(Long chatId, String data, TelegramLongPollingBot bot) {

        if (data.equals("ignore")) return;

        YearMonth currentMonth = currentMonthMap.get(chatId);
        LocalDate start = startDateMap.get(chatId);
        LocalDate end = endDateMap.get(chatId);

        if (data.startsWith("month_")) {
            YearMonth month = YearMonth.parse(data.replace("month_", ""));
            currentMonthMap.put(chatId, month);

            update(chatId, bot, "Выбери дату 👇", month, start, end);
            return;
        }

        if (data.startsWith("date_")) {
            LocalDate date = LocalDate.parse(data.replace("date_", ""));

            if (start == null) {
                startDateMap.put(chatId, date);

                update(chatId, bot,
                        "Начало: " + date + "\nВыбери конец 👇",
                        currentMonth,
                        date,
                        null
                );
                return;
            }

            endDateMap.put(chatId, date);
            end = date;

            if (end.isBefore(start)) {
                LocalDate tmp = start;
                start = end;
                end = tmp;
            }

// 🔥 СНАЧАЛА ОБНОВЛЯЕМ КАЛЕНДАРЬ (ПОКАЗЫВАЕМ ДИАПАЗОН)
            update(chatId, bot,
                    "Выбран диапазон:\n" + start + " — " + end,
                    currentMonth,
                    start,
                    end
            );

// ❗ ВРЕМЕННО УБЕРИ ЭТУ СТРОКУ
// processResult(chatId, bot, start, end);
        }
    }

    private void processResult(Long chatId,
                               TelegramLongPollingBot bot,
                               LocalDate start,
                               LocalDate end) {

        Long playerId = selectedPlayerId.get(chatId);
        Player player = null;

        // админ
        if (playerId != null) {
            player = playerService.findById(playerId);
        }

        // пользователь
        if (player == null) {
            Long telegramId = telegramIdMap.get(chatId);
            if (telegramId != null) {
                player = playerService.getByTelegramId(telegramId);
            }
        }

        String state = userState.get(chatId);

        if (player == null || state == null) {
            messageService.send(bot, chatId, "❌ Ошибка состояния");
            cleanup(chatId);
            return;
        }

        if (state.contains("TOURNAMENTS")) {
            var results = tournamentResultService.getResultsByPeriod(player, start, end);

            StringBuilder sb = new StringBuilder("📅 Турниры:\n\n");
            results.forEach(r ->
                    sb.append(r.getDate())
                            .append(" — ")
                            .append(r.getAmount())
                            .append("\n")
            );

            messageService.send(bot, chatId, sb.toString());
        }

        if (state.contains("SUM")) {
            var stats = tournamentResultService.getStatsByPeriod(player, start, end);

            String response =
                    "💰 Сумма: " + stats.getSum() +
                            "\n📊 Среднее: " + stats.getAverage() +
                            "\n💸 Сумма -3%: " + stats.getMinusThreePercent();

            messageService.send(bot, chatId, response);
        }

        cleanup(chatId);

        // вернуть меню
        Long telegramId = telegramIdMap.get(chatId);
        if (telegramId != null) {
            messageService.sendMenu(bot, chatId, telegramId);
        }
    }

    private void update(Long chatId,
                        TelegramLongPollingBot bot,
                        String text,
                        YearMonth month,
                        LocalDate start,
                        LocalDate end) {

        try {
            Integer messageId = calendarMessageId.get(chatId);
            if (messageId == null) return;

            var edit = new EditMessageText();
            edit.setChatId(chatId.toString());
            edit.setMessageId(messageId);
            edit.setText(text);
            edit.setReplyMarkup(CalendarKeyboardBuilder.build(month, start, end));

            bot.execute(edit);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔥 ВОТ ЭТО КЛЮЧЕВОЙ МЕТОД (фикс бага)
    public void reset(Long chatId) {
        cleanup(chatId);
    }

    // 🔥 очистка состояния
    private void cleanup(Long chatId) {
        userState.remove(chatId);
        selectedPlayerId.remove(chatId);
        startDateMap.remove(chatId);
        endDateMap.remove(chatId); // 🔥 ВОТ ЭТА СТРОКА
        currentMonthMap.remove(chatId);
        calendarMessageId.remove(chatId);
        telegramIdMap.remove(chatId);
    }
}