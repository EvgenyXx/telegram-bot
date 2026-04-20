package com.example.parser.modules.notification.formatter;

import com.example.parser.modules.notification.domain.PlayerNotification;
import org.springframework.stereotype.Component;

@Component
public class TournamentCancelledMessageBuilder {

    public String build(PlayerNotification pn) {
        var t = pn.getTournament();

        return "❌ Турнир отменен\n\n" +
                "Дата и время:\n" +
                (t.getDate() != null ? "📅 " + t.getDate() + "\n" : "") +
                (t.getTime() != null ? "🕒 " + t.getTime() + "\n" : "") +
                (pn.getHall() != null ? "\n🏟 Зал: " + pn.getHall() + "\n" : "") +
                "\n📎 Подробнее: " + t.getLink();
    }
}