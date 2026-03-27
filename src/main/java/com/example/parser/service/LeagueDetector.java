package com.example.parser.service;

import com.example.parser.model.LeagueType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class LeagueDetector {

    public LeagueType detectLeague(String url) throws Exception {

        System.out.println("DETECTING LEAGUE FROM URL: " + url);

        Document doc = Jsoup.connect(url).get();
        String title = doc.title();

        System.out.println("PAGE TITLE: " + title);

        if (title.contains("Лига A") || title.contains("Лига А")) return LeagueType.A;
        if (title.contains("Лига В") || title.contains("Лига B")) return LeagueType.B;
        if (title.contains("Лига С") || title.contains("Лига C")) return LeagueType.C;
        if (title.contains("Лига D")) return LeagueType.D;

        throw new RuntimeException("Не удалось определить лигу");
    }
}