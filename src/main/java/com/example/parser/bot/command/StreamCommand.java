package com.example.parser.bot.command;

import com.example.parser.notification.LiveTournamentService;
import com.example.parser.notification.MessageService;
import com.example.parser.player.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class StreamCommand implements CommandHandler {

    private final LiveTournamentService liveTournamentService;
    private final MessageService messageService;

    private static final String COMMAND = "/stream";

    @Override
    public boolean supports(String text, Player player) {
        return COMMAND.equals(text);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) {
        Long chatId = update.getMessage().getChatId();

        log.info("User requested tournaments, chatId={}", chatId);

        List<Integer> halls = List.of(10, 11);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Integer hall : halls) {
            String url = liveTournamentService.getLiveByHall(hall);

            InlineKeyboardButton button = new InlineKeyboardButton();

            if (url != null) {
                button.setText("🏓 Стол " + hall);
                button.setUrl(url);

                log.debug("Hall {} -> tournament found: {}", hall, url);
            } else {
                button.setText("❌ Стол " + hall + " (нет турнира)");
                button.setCallbackData("no_tournament");

                log.warn("Hall {} -> no tournament available", hall);
            }

            rows.add(List.of(button));
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);

        messageService.sendInlineKeyboard(
                bot,
                chatId,
                "🏓 Выбери стол:",
                keyboard
        );
    }
}