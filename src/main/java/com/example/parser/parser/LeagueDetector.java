package com.example.parser.parser;

import com.example.parser.domain.model.LeagueType;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeagueDetector {



    public LeagueType detectLeague(Document doc) throws Exception {

        String title = doc.title();

        System.out.println("PAGE TITLE: " + title);

        if (title.contains("Лига A") || title.contains("Лига А")) return LeagueType.A;
        if (title.contains("Лига В") || title.contains("Лига B")) return LeagueType.B;
        if (title.contains("Лига С") || title.contains("Лига C")) return LeagueType.C;
        if (title.contains("Лига D")) return LeagueType.D;

        throw new RuntimeException("Не удалось определить лигу");
    }
}