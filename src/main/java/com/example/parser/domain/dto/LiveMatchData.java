package com.example.parser.domain.dto;

import com.example.parser.domain.model.Match;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LiveMatchData {
    private Match match;
    private boolean finished;
    private Match lastMatch;
}