package com.example.parser.bot.command;

import com.example.parser.domain.entity.Lineup;
import com.example.parser.lineup.LineupMessageBuilder;
import com.example.parser.lineup.LineupQueryService;
import com.example.parser.player.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
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
            sendLineupsFile(chatId, bot);
        } catch (Exception e) {
            log.error("Error sending lineups file", e);
        }
    }

    private void sendLineupsFile(Long chatId, TelegramLongPollingBot bot) throws Exception {

        // 👉 теперь берём НЕ текст, а список
        List<Lineup> lineups = lineupQueryService.getTomorrowLineups();

        if (lineups == null || lineups.isEmpty()) {
            bot.execute(new SendMessage(chatId.toString(), "❌ Нет составов на завтра"));
            return;
        }

        // 👉 билдим файл через сервис
        InputFile file = lineupMessageBuilder.buildTomorrowFile(lineups);

        if (file == null) {
            bot.execute(new SendMessage(chatId.toString(), "❌ Ошибка формирования файла"));
            return;
        }

        SendDocument doc = new SendDocument();
        doc.setChatId(chatId.toString());
        doc.setDocument(file);

        bot.execute(doc);
    }
}