package com.example.parser;

import com.example.parser.domain.entity.Lineup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;


@Service
public class LineupMessageBuilder {

    public String buildTomorrowMessage(List<Lineup> lineups) {

        if (lineups.isEmpty()) {
            return "❌ Нет составов на завтра";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📋 Ростов — составы на завтра\n\n");

        lineups.stream()
                .sorted(Comparator.comparing(Lineup::getTime))
                .forEach(l -> {
                    sb.append("Лига ")
                            .append(l.getLeague())
                            .append(" | ")
                            .append(l.getTime())
                            .append(" — ")
                            .append(l.getPlayers())
                            .append("\n");
                });

        return sb.toString();
    }
}