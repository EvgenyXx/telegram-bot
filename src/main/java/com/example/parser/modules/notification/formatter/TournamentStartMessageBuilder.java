package com.example.parser.modules.notification.formatter;

import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.tournament.persistence.entity.TournamentEntity;
import org.springframework.stereotype.Component;

@Component
public class TournamentStartMessageBuilder {

    public String build(PlayerNotification pn) {

        TournamentEntity t = pn.getTournament();

        return "🚀 Турнир начался!\n\n"
                + "📅 Дата: " + t.getDate() + "\n"
                + "⏰ Время: " + t.getTime() + "\n"
                + "🔗 " + t.getLink() + "\n\n"
                + "📊 Результаты будут автоматически посчитаны и добавлены в твои турниры";
    }
}