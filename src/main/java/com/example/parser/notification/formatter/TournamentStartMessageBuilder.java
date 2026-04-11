package com.example.parser.notification.formatter;

import com.example.parser.domain.entity.PlayerNotification;
import org.springframework.stereotype.Component;

@Component
public class TournamentStartMessageBuilder {

    public String build(PlayerNotification pn) {
        return "🚀 Турнир начался!\n\n"
                + "📅 Дата: " + pn.getDate() + "\n"
                + "⏰ Время: " + pn.getTime() + "\n"
                + "🔗 " + pn.getLink() + "\n\n"
                + "📊 Результаты будут автоматически посчитаны и добавлены в твои турниры";
    }
}