package ru.pulsecore.app.modules.lineup.mapper;

import ru.pulsecore.app.core.dto.TournamentDto;
import ru.pulsecore.app.modules.lineup.domain.Lineup;
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