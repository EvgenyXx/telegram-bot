package com.example.parser.bot.handler;

import com.example.parser.bot.command.CommandRouter;
import com.example.parser.modules.tournament.calendar.service.CalendarResultService;
import com.example.parser.modules.tournament.calendar.domain.CalendarSession;
import com.example.parser.modules.tournament.calendar.domain.CalendarState;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageRouter {

    private final CallbackHandler callbackHandler;
    private final CommandRouter commandRouter;
    private final TextHandler textHandler;
    private final CalendarResultService calendarResultService;

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        // WEB APP
        if (update.hasMessage() && update.getMessage().getWebAppData() != null) {
            handleWebApp(update, bot);
            return;
        }

        // CALLBACK
        if (update.hasCallbackQuery()) {
            callbackHandler.handle(update, bot);
            return;
        }

        // TEXT
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.debug("TEXT: {}", update.getMessage().getText());
            handleText(update, bot);
            return;
        }

        // UNKNOWN
        log.warn("Unknown update type received: {}", update);
    }

    private void handleWebApp(Update update, TelegramLongPollingBot bot) {
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();
        String data = update.getMessage().getWebAppData().getData();

        log.debug("WebApp data received: chatId={}, data={}", chatId, data);

        try {
            Map<String, String> map = new ObjectMapper().readValue(data, Map.class);

            LocalDate start = LocalDate.parse(map.get("start"));
            LocalDate end = LocalDate.parse(map.get("end"));
            CalendarState state = CalendarState.valueOf(map.get("state"));

            CalendarSession session = new CalendarSession();
            session.setStart(start);
            session.setEnd(end);
            session.setState(state);
            session.setTelegramId(telegramId);

            calendarResultService.processResult(chatId, session, bot);

            log.debug("WebApp processed: chatId={}, state={}", chatId, state);

        } catch (Exception e) {
            log.error("Failed to process WebApp data: {}", data, e);
        }
    }

    private void handleText(Update update, TelegramLongPollingBot bot) throws Exception {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        log.debug("Text received: chatId={}, text={}", chatId, text);

        if (textHandler.handle(chatId, text, bot)) {
            return;
        }

        commandRouter.handle(update, bot);
    }
}