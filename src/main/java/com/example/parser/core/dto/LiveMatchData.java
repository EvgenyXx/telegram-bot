package com.example.parser.core.dto;

import com.example.parser.core.model.Match;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LiveMatchData {
    private Match match;
    private boolean finished;
    private Match lastMatch;
}