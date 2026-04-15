package com.example.parser.lineup;

import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.domain.entity.Lineup;
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
                .city("Ростов")
                .build();
    }
}