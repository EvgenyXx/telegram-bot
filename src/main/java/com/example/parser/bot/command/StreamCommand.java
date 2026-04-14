package com.example.parser.bot.command;


import com.example.parser.LineupQueryService;
import com.example.parser.player.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@Slf4j
@RequiredArgsConstructor
public class StreamCommand implements CommandHandler {

    private final LineupQueryService lineupQueryService;

    private static final String COMMAND = "/stream";

    @Override
    public boolean supports(String text, Player player) {
        return COMMAND.equals(text);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) {
        Long chatId = update.getMessage().getChatId();

        log.info("User requested lineups, chatId={}", chatId);

        String text = lineupQueryService.getTomorrowMessage();

        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(text);

            bot.execute(message);

        } catch (Exception e) {
            log.error("Error sending lineups", e);
        }
    }
}