package com.example.parser.notification.formatter;

import com.example.parser.domain.entity.PlayerNotification;
import org.springframework.stereotype.Component;

@Component
public class TournamentCancelledMessageBuilder {

    public String build(PlayerNotification pn) {
        return "❌ Турнир отменен\n\n" +
                "📎 " + pn.getTournament().getLink();
    }
}