package com.example.parser.modules.lineup.mapper;

import com.example.parser.core.dto.TournamentDto;
import com.example.parser.modules.lineup.domain.Lineup;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class LineupMapper {

    public Lineup toEntity(TournamentDto t, LocalDate date, String time) {
        return Lineup.builder()
                .league(t.getLeague())
                .time(time)
                .players(String.join(", ", t.getPlayers()))
                .date(date)
                .build();
    }
}