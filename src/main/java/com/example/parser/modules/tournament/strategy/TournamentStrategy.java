package com.example.parser.modules.tournament.strategy;

import com.example.parser.core.model.Match;
import com.example.parser.modules.tournament.service.result.ParsedResult;
import com.example.parser.modules.tournament.service.result.ResultService;
import org.jsoup.nodes.Document;

import java.util.List;

public interface TournamentStrategy {

    boolean isApplicable(Document doc, List<Match> matches);

    ParsedResult calculate(Document doc, List<Match> matches) throws Exception;
}