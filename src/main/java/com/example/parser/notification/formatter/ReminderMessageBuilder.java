package com.example.parser.notification.formatter;

import com.example.parser.domain.entity.PlayerNotification;
import org.springframework.stereotype.Component;

@Component
public class ReminderMessageBuilder {

    public String buildEvening(PlayerNotification pn) {
        return "🌙 Напоминание на завтра\n\n" +
                "🏓 У тебя турнир утром\n" +
                "📅 " + pn.getDate() + "\n" +
                "🕒 " + pn.getTime() + "\n\n" +
                "Подготовься заранее 💪";
    }

    public String buildHour(PlayerNotification pn) {
        return "⏰ Через 1 час турнир\n\n" +
                "📅 " + pn.getDate() + "\n" +
                "🕒 " + pn.getTime() + "\n" +
                "🔗 " + pn.getLink();
    }
}