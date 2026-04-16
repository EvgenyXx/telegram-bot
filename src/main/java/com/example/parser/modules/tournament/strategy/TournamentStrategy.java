package com.example.parser.modules.tournament.strategy;

import com.example.parser.core.model.Match;
import com.example.parser.modules.tournament.service.ResultService;
import org.jsoup.nodes.Document;

import java.util.List;

public interface TournamentStrategy {

    boolean isApplicable(Document doc, List<Match> matches);

    ResultService.ParsedResult calculate(Document doc, List<Match> matches) throws Exception;
}