package com.example.parser.service;

import com.example.parser.LeagueType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class LeagueDetector {

    public LeagueType detectLeague(String url) throws Exception {

        Document doc = Jsoup.connect(url).get();
        String title = doc.title();

        if (title.contains("Лига А")) return LeagueType.A;
        if (title.contains("Лига В")) return LeagueType.B;
        if (title.contains("Лига С")) return LeagueType.C;
        if (title.contains("Лига D")) return LeagueType.D;

        throw new RuntimeException("Не удалось определить лигу");
    }
}