package com.example.parser.bot.command;

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

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Component
@Slf4j
@RequiredArgsConstructor
public class StreamCommand implements CommandHandler {

    private final LineupQueryService lineupQueryService;

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

        // 👉 получаем текст расписания
        String text = lineupQueryService.getTomorrowMessage();

        if (text == null || text.isBlank()) {
            bot.execute(new SendMessage(chatId.toString(), "❌ Нет составов на завтра"));
            return;
        }

        // 👉 превращаем текст в байты
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);

        // 👉 создаём файл
        InputFile file = new InputFile(
                new ByteArrayInputStream(bytes),
                buildFileName()
        );

        // 👉 отправка файла
        SendDocument doc = new SendDocument();
        doc.setChatId(chatId.toString());
        doc.setDocument(file);

        bot.execute(doc);
    }

    private String buildFileName() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        return "lineups_" + tomorrow + ".txt";
    }
}