package com.example.parser.modules.notification.start;

import com.example.parser.modules.tournament.domain.Tournament;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class TournamentTimeService {

    private static final ZoneId ZONE = ZoneId.of("Europe/Moscow");

    public boolean isStartedByTime(Tournament t) {
        if (t.getDate() == null || t.getTime() == null) return false;

        ZonedDateTime start = ZonedDateTime.of(
                t.getDate(),
                LocalTime.parse(t.getTime()),
                ZONE
        );

        return ZonedDateTime.now(ZONE).isAfter(start);
    }

    public boolean isToday(Tournament t) {
        return t.getDate() != null &&
                t.getDate().isEqual(ZonedDateTime.now(ZONE).toLocalDate());
    }

//    public boolean isNearStart(Tournament t) {
//        if (t.getDate() == null || t.getTime() == null) return false;
//
//        ZonedDateTime now = ZonedDateTime.now(ZONE);
//        ZonedDateTime start = ZonedDateTime.of(
//                t.getDate(),
//                LocalTime.parse(t.getTime()),
//                ZONE
//        );
//
//        long minutes = Math.abs(java.time.Duration.between(now, start).toMinutes());
//        return minutes <= 5;
//    }
}