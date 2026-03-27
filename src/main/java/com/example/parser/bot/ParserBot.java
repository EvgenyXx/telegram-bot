package com.example.parser.bot;

import com.example.parser.service.ResultService;
import com.example.parser.dto.ResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ParserBot extends TelegramLongPollingBot {

    private final ResultService resultService;

    @Value("${bot.token}")
    private String token;



    @Override
    public String getBotUsername() {
        return "@table_tennis_parser_bot";
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {

            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            try {

                // 👉 если просто ссылка — считаем весь турнир
                if (text.startsWith("http")) {

                    List<ResultDto> results = resultService.calculateAll(text);

                    StringBuilder sb = new StringBuilder();
                    sb.append("🏆 Результаты турнира:\n\n");

                    int i = 1;
                    String date = results.isEmpty() ? null : results.get(0).getDate();
                    for (ResultDto r : results) {
                        sb.append(date)
                                .append(i++)
                                .append(". ")
                                .append(r.getPlayer())
                                .append(" — ")
                                .append(r.getTotal())
                                .append("\n");
                    }

                    execute(sendText(chatId, sb.toString()));
                    return;
                }

                // 👉 если не ссылка
                execute(sendText(chatId, "Скинь ссылку на турнир 👇"));

            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    private SendMessage sendText(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        return message;
    }
}