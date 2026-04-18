package com.example.parser.bot.command;

import com.example.parser.bot.handler.CommandHandler;
import com.example.parser.modules.lineup.domain.Lineup;
import com.example.parser.modules.lineup.api.LineupMessageBuilder;
import com.example.parser.modules.lineup.service.LineupQueryService;
import com.example.parser.modules.player.domain.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class LineupCommand implements CommandHandler {

    private final LineupQueryService lineupQueryService;
    private final LineupMessageBuilder lineupMessageBuilder;

    private static final String COMMAND = "/lineup";

    @Override
    public boolean supports(String text, Player player) {
        return COMMAND.equals(text);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) {
        Long chatId = update.getMessage().getChatId();

        log.info("User requested lineups, chatId={}", chatId);

        try {
            sendLineupsMessage(chatId, bot);
        } catch (Exception e) {
            log.error("Error sending lineups", e);
        }
    }

    private void sendLineupsMessage(Long chatId, TelegramLongPollingBot bot) throws Exception {
        List<Lineup> lineups = lineupQueryService.getTomorrowLineups();

        if (lineups == null || lineups.isEmpty()) {
            bot.execute(new SendMessage(chatId.toString(), "❌ Нет составов на завтра"));
            return;
        }

        // 🔥 ВОТ ГЛАВНОЕ — просто отправка файла
        lineupMessageBuilder.sendLineups(bot, chatId, lineups);
    }
}