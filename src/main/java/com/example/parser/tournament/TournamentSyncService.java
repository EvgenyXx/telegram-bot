package com.example.parser.tournament;

import com.example.parser.lineup.TournamentRepository;
import com.example.parser.domain.entity.Tournament;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TournamentSyncService {

    private final TournamentRepository tournamentRepository;

    public Tournament sync(ResultService.ParsedResult parsed, String link) {
        Tournament t = tournamentRepository
                .findByExternalId(parsed.getTournamentId())
                .orElseGet(Tournament::new);

        t.setExternalId(parsed.getTournamentId());
        t.setLink(link);

        // ✅ корректные флаги
        t.setFinished(parsed.isFinished());
        t.setStarted(true); // можно, если есть результаты

        // ✅ дата
        if (!parsed.getResults().isEmpty()) {
            t.setDate(LocalDate.parse(parsed.getResults().get(0).getDate()));
        }

        return tournamentRepository.save(t);
    }
}