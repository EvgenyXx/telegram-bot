package ru.pulsecore.app.modules.lineup.service;

import ru.pulsecore.app.modules.lineup.domain.Lineup;
import ru.pulsecore.app.modules.lineup.repository.LineupRepository;
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