package com.example.parser.notification.formatter;

import com.example.parser.domain.dto.TournamentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
@RequiredArgsConstructor
public class TournamentDiscoveryMessageFormatter {

    private final TournamentMessageBuilder messageBuilder;

    public String format(List<TournamentDto> tournaments) {

        String title = "🔥 Турнир:\n\n";

        StringBuilder msg = new StringBuilder(title);

        for (TournamentDto t : tournaments) {
            msg.append(messageBuilder.build(t));
        }

        return msg.toString();
    }
}