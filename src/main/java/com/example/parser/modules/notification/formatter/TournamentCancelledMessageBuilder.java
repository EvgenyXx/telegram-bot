package com.example.parser.modules.notification.formatter;

import com.example.parser.modules.notification.domain.PlayerNotification;
import org.springframework.stereotype.Component;

@Component
public class TournamentCancelledMessageBuilder {

    public String build(PlayerNotification pn) {
        return "❌ Турнир отменен\n\n" +
                "📎 " + pn.getTournament().getLink();
    }
}