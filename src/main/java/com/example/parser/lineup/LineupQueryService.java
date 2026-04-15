package com.example.parser.lineup;

import com.example.parser.domain.entity.Lineup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LineupQueryService {

    private final LineupRepository lineupRepository;
    private final LineupMessageBuilder messageBuilder;

    // 👉 старый метод (оставляем)


    // 👉 НОВЫЙ метод (ключевой)
    public List<Lineup> getTomorrowLineups() {
        return lineupRepository.findByDate(LocalDate.now().plusDays(1));
    }
}