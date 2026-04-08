package com.example.parser.tournament;

import com.example.parser.domain.model.Match;
import org.jsoup.nodes.Document;

import java.util.List;

public interface TournamentStrategy {

    boolean isApplicable(Document doc, List<Match> matches);

    ResultService.ParsedResult calculate(Document doc, List<Match> matches) throws Exception;
}