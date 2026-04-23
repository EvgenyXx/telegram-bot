package com.example.parser.modules.tournament.application;

import com.example.parser.modules.tournament.persistence.repository.TournamentRepository;
import com.example.parser.modules.tournament.persistence.entity.TournamentEntity;
import com.example.parser.modules.tournament.mapper.TournamentStatusMapper;
import com.example.parser.modules.tournament.domain.ParsedResult;
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