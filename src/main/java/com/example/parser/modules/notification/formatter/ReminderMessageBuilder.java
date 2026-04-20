package com.example.parser.modules.notification.formatter;

import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.tournament.domain.TournamentEntity;
import org.springframework.stereotype.Component;

@Component
public class ReminderMessageBuilder {

    public String buildEvening(PlayerNotification pn) {

        TournamentEntity t = pn.getTournament();

        return "🌙 Напоминание на завтра\n\n" +
                "🏓 У тебя турнир утром\n" +
                "📅 " + t.getDate() + "\n" +
                "🕒 " + t.getTime() + "\n\n" +
                "Подготовься заранее 💪";
    }

    public String buildHour(PlayerNotification pn) {

        TournamentEntity t = pn.getTournament();

        return "⏰ Через 1 час турнир\n\n" +
                "📅 " + t.getDate() + "\n" +
                "🕒 " + t.getTime() + "\n" +
                "🔗 " + t.getLink();
    }
}