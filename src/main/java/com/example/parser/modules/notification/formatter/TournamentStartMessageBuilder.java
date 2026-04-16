package com.example.parser.modules.notification.formatter;

import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.tournament.domain.Tournament;
import org.springframework.stereotype.Component;

@Component
public class TournamentStartMessageBuilder {

    public String build(PlayerNotification pn) {

        Tournament t = pn.getTournament();

        return "🚀 Турнир начался!\n\n"
                + "📅 Дата: " + t.getDate() + "\n"
                + "⏰ Время: " + t.getTime() + "\n"
                + "🔗 " + t.getLink() + "\n\n"
                + "📊 Результаты будут автоматически посчитаны и добавлены в твои турниры";
    }
}