package com.example.parser.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
public class ParserBot extends TelegramLongPollingBot {


//    public ParserBot() {
//        super(createOptions());
//    }
//
//    private static DefaultBotOptions createOptions() {
//        DefaultBotOptions options = new DefaultBotOptions();
//
//        options.setProxyHost("t.neodon-vpn.com");
//        options.setProxyPort(443);
//        options.setProxyType(DefaultBotOptions.ProxyType.);
//
//        return options;
//    }

    @Override
    public String getBotUsername() {
        return "@table_tennis_parser_bot";
    }

    @Override
    public String getBotToken() {
        return "8661205196:AAFQmU-k-KzhstN4Kdd8ucL08uVg0GqaG-0";
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {

            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());

            message.setText("Ты написал: " + text);

            try {
                execute(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}