package com.example.parser.parser;

import com.example.parser.domain.model.Match;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TournamentStatusResolver {

    public TournamentStatus resolve(Document doc, List<Match> matches) {

        boolean hasCompleted = matches.stream()
                .anyMatch(m -> m.getScore1() != 0 || m.getScore2() != 0);

        boolean hasCanceled = !doc.select(".canceled").isEmpty();
        boolean hasRemoved = !doc.select(".removed").isEmpty();

        // ❌ вообще ничего не сыграли
        if (!hasCompleted && hasCanceled) {
            return TournamentStatus.CANCELED;
        }

        // ⚠️ твой главный кейс
        if (hasCompleted && hasRemoved) {
            return TournamentStatus.WITHDRAWN_PLAYER;
        }

        // ✅ норм турнир
        return TournamentStatus.NORMAL;
    }
}