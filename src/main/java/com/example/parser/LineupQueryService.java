package com.example.parser;

import com.example.parser.repository.LineupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LineupQueryService {

    private final LineupRepository lineupRepository;
    private final LineupMessageBuilder messageBuilder;

    public String getTomorrowMessage() {
        var lineups = lineupRepository.findByDate(LocalDate.now().plusDays(1));
        return messageBuilder.buildTomorrowMessage(lineups);
    }
}