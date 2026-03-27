package com.example.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MatchParser {

    public List<Match> parseMatches(String url) throws Exception {

        Document doc = Jsoup.connect(url).get();
        List<Match> matches = new ArrayList<>();

        Elements rows = doc.select(".ml_tour_game_list_row");

        for (Element row : rows) {

            Elements cols = row.select(".ml_tour_game_list_col");

            if (cols.size() < 6) continue;

            String stage = cols.get(0).text();
            String player1 = cols.get(3).text();
            String scoreText = cols.get(4).text();
            String player2 = cols.get(5).text();

            // пропускаем если нет счета
            if (!scoreText.contains(":")) continue;

            // убираем всё после "("
            scoreText = scoreText.split("\\(")[0];

// теперь нормальный счет
            String[] scoreParts = scoreText.split(":");

            int a = Integer.parseInt(scoreParts[0].trim());
            int b = Integer.parseInt(scoreParts[1].trim());

            Match match = new Match();
            match.setStage(stage);
            match.setPlayer1(player1);
            match.setPlayer2(player2);
            match.setScore1(a);
            match.setScore2(b);

            matches.add(match);
        }

        return matches;
    }
}