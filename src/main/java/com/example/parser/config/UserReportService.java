package com.example.parser.config;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


@Component
public class UserReportService {

    public File buildReport(Update update) throws IOException {

        var msg = update.getMessage();
        var user = msg.getFrom();
        var chat = msg.getChat();

        String content =
                "📄 USER INFO\n\n" +

                        "🆔 ID: " + user.getId() + "\n" +
                        "👤 Username: @" + safe(user.getUserName()) + "\n" +
                        "📛 First name: " + safe(user.getFirstName()) + "\n" +
                        "📛 Last name: " + safe(user.getLastName()) + "\n" +
                        "🌐 Language: " + safe(user.getLanguageCode()) + "\n" +
                        "💎 Premium: " + safe(user.getIsPremium()) + "\n" +
                        "🤖 Is bot: " + user.getIsBot() + "\n\n" +

                        "💬 CHAT INFO\n\n" +

                        "💬 Chat ID: " + chat.getId() + "\n" +
                        "📂 Chat type: " + chat.getType() + "\n\n" +

                        "📨 MESSAGE INFO\n\n" +

                        "✉️ Text: " + safe(msg.getText()) + "\n" +
                        "🕒 Date: " + msg.getDate() + "\n" +
                        "📎 Has callback: " + (update.getCallbackQuery() != null);

        File file = File.createTempFile("user_" + user.getId() + "_", ".txt");

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }

        return file;
    }

    private String safe(Object value) {
        return value == null ? "нет" : value.toString();
    }
}