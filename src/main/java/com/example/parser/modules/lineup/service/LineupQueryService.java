package com.example.parser.modules.lineup.service;

import com.example.parser.modules.lineup.domain.Lineup;
import com.example.parser.modules.lineup.repository.LineupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LineupQueryService {

    private final LineupRepository lineupRepository;

    // 👉 НОВЫЙ метод (ключевой)
    public List<Lineup> getTomorrowLineups() {
        return lineupRepository.findByDate(LocalDate.now().plusDays(1));
    }
}