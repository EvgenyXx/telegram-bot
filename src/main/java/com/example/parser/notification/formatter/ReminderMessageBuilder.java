package com.example.parser.notification.formatter;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.domain.entity.Tournament;
import org.springframework.stereotype.Component;

@Component
public class ReminderMessageBuilder {

    public String buildEvening(PlayerNotification pn) {

        Tournament t = pn.getTournament();

        return "🌙 Напоминание на завтра\n\n" +
                "🏓 У тебя турнир утром\n" +
                "📅 " + t.getDate() + "\n" +
                "🕒 " + t.getTime() + "\n\n" +
                "Подготовься заранее 💪";
    }

    public String buildHour(PlayerNotification pn) {

        Tournament t = pn.getTournament();

        return "⏰ Через 1 час турнир\n\n" +
                "📅 " + t.getDate() + "\n" +
                "🕒 " + t.getTime() + "\n" +
                "🔗 " + t.getLink();
    }
}