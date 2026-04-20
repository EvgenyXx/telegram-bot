package com.example.parser.modules.notification.start;

import com.example.parser.modules.tournament.domain.TournamentEntity;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class TournamentTimeService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    public boolean isStartedByTime(TournamentEntity t) {
        if (t.getDate() == null || t.getTime() == null) return false;

        ZonedDateTime start = ZonedDateTime.of(
                t.getDate(),
                LocalTime.parse(t.getTime()),
                ZONE
        );

        return ZonedDateTime.now(ZONE).isAfter(start);
    }

    public boolean isToday(TournamentEntity t) {
        return t.getDate() != null &&
                t.getDate().isEqual(ZonedDateTime.now(ZONE).toLocalDate());
    }


}