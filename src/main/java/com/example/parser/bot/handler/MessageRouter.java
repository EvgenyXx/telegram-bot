package com.example.parser.bot.handler;

import com.example.parser.bot.command.CommandRouter;
import com.example.parser.tournament.calendar.CalendarResultService;
import com.example.parser.tournament.calendar.CalendarSession;
import com.example.parser.tournament.calendar.CalendarState;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MessageRouter {

    private final CallbackHandler callbackHandler;
    private final CommandRouter commandRouter;
    private final TextHandler textHandler;
    private final CalendarResultService calendarResultService; // 👈 ДОБАВЬ

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        // 🔥 ВОТ ОНО
        if (update.hasMessage() && update.getMessage().getWebAppData() != null) {

            Long chatId = update.getMessage().getChatId();
            Long telegramId = update.getMessage().getFrom().getId();

            String data = update.getMessage().getWebAppData().getData();

            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> map = mapper.readValue(data, Map.class);

            LocalDate start = LocalDate.parse(map.get("start"));
            LocalDate end = LocalDate.parse(map.get("end"));
            CalendarState state = CalendarState.valueOf(map.get("state"));

            CalendarSession session = new CalendarSession();
            session.setStart(start);
            session.setEnd(end);
            session.setState(state);
            session.setTelegramId(telegramId);

            calendarResultService.processResult(chatId, session, bot);

            return;
        }

        if (update.hasCallbackQuery()) {
            callbackHandler.handle(update, bot);
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            if (textHandler.handle(chatId, text, bot)) {
                return;
            }

            commandRouter.handle(update, bot);
        }
    }
}