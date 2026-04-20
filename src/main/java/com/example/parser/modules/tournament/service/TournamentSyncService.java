package com.example.parser.modules.tournament.service;

import com.example.parser.modules.tournament.repository.TournamentRepository;
import com.example.parser.modules.tournament.domain.TournamentEntity;
import com.example.parser.modules.tournament.service.result.ParsedResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TournamentSyncService {

    private final TournamentRepository tournamentRepository;
    private final TournamentStatusMapper statusMapper;

    public TournamentEntity sync(ParsedResult parsed, String link) {
        TournamentEntity t = tournamentRepository
                .findByExternalId(parsed.getTournamentId())
                .orElseGet(TournamentEntity::new);

        t.setExternalId(parsed.getTournamentId());
        t.setLink(link);

        // ✅ корректные флаги
       statusMapper.apply(t,parsed.getStatus());

        // ✅ дата
        if (!parsed.getResults().isEmpty()) {
            t.setDate(LocalDate.parse(parsed.getResults().get(0).getDate()));
        }

        return tournamentRepository.save(t);
    }
}