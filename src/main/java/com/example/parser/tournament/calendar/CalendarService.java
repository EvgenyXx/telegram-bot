package com.example.parser.tournament.calendar;

import com.example.parser.bot.keyboard.CalendarKeyboardBuilder;
import com.example.parser.notification.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CalendarSessionService sessionService;
    private final CalendarResultService resultService;
    private final MessageService messageService;

    public void open(Long chatId, Long telegramId, TelegramLongPollingBot bot) {
        CalendarSession session = sessionService.get(chatId);

        session.setTelegramId(telegramId);
        session.setCurrentMonth(YearMonth.now());

        var msg = messageService.sendInlineKeyboardAndGetMessage(
                bot,
                chatId,
                "Выбери дату начала 👇",
                CalendarKeyboardBuilder.build(session.getCurrentMonth(), null, null)
        );

        session.setMessageId(msg.getMessageId());
    }

    public void setState(Long chatId, CalendarState state) {
        CalendarSession session = sessionService.get(chatId);
        session.setState(state);
    }

    public void handleCallback(Long chatId, String data, TelegramLongPollingBot bot) {

        if (data.equals("ignore")) return;

        CalendarSession session = sessionService.get(chatId);

        if (data.startsWith("month_")) {
            YearMonth month = YearMonth.parse(data.replace("month_", ""));
            session.setCurrentMonth(month);

            update(chatId, bot, "Выбери дату 👇", session);
            return;
        }

        if (data.startsWith("date_")) {
            LocalDate date = LocalDate.parse(data.replace("date_", ""));

            if (session.getStart() == null) {
                session.setStart(date);
                update(chatId, bot, "Начало: " + date + "\nВыбери конец 👇", session);
                return;
            }

            session.setEnd(date);

            if (session.getEnd().isBefore(session.getStart())) {
                LocalDate tmp = session.getStart();
                session.setStart(session.getEnd());
                session.setEnd(tmp);
            }

            update(chatId, bot,
                    "Выбран диапазон:\n" + session.getStart() + " — " + session.getEnd(),
                    session
            );

            resultService.processResult(chatId, session, bot);
            sessionService.remove(chatId);
        }
    }

    private void update(Long chatId,
                        TelegramLongPollingBot bot,
                        String text,
                        CalendarSession session) {

        try {
            if (session.getMessageId() == null) return;

            var edit = new EditMessageText();
            edit.setChatId(chatId.toString());
            edit.setMessageId(session.getMessageId());
            edit.setText(text);
            edit.setReplyMarkup(
                    CalendarKeyboardBuilder.build(
                            session.getCurrentMonth(),
                            session.getStart(),
                            session.getEnd()
                    )
            );

            bot.execute(edit);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}