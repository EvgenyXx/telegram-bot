package com.example.parser.modules.tournament.service.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class MatchProcessingResult {

    private Map<String, Integer> pointsMap;
    private Map<String, Integer> placeMap;
}