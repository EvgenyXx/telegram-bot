package com.example.parser.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

// 👉 ИМПОРТИРУЙ СВОИ КЛАССЫ
// import com.example.parser.domain.Match;
// import com.example.parser.service..PointsCalculator;

public class TestApiMain {

    public static void main(String[] args) throws Exception {

        String link = "https://masters-league.com/tours/liga-v-7189/";

        Document document = Jsoup.connect(link).get();

        for (Element row : document.select(".ml_tour_game_list_row")) {

            Elements players = row.select(".ml_tour_game_plr ");

            {

                for (Element player : players) {

                    String name = player.text();
                    boolean isRemoved = player.hasClass("removed");
                    if (isRemoved) {
                        System.out.println("игрок снялся " + name);
                    }
                }
            }
        }
    }
}