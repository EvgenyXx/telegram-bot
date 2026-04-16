package com.example.parser.modules.notification.service;

import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.tournament.domain.Tournament;
import com.example.parser.modules.notification.repository.PlayerNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LiveTournamentService {

    private final PlayerNotificationRepository repository;

    public String getLiveByHall(int hall) {
        log.info("Request live stream for hall={}", hall);

        List<PlayerNotification> today =
                repository.findTodayWithTournament(LocalDate.now());

        log.debug("Found {} tournaments for today", today.size());

        List<PlayerNotification> filtered = today.stream()
                .filter(p -> isHallMatch(p, hall))
                .toList();

        PlayerNotification current = findCurrent(filtered);

        if (current != null) {
            Tournament t = current.getTournament();

            log.info("Active tournament found: id={}, time={}, link={}",
                    t.getExternalId(),
                    t.getTime(),
                    t.getLink());

            return t.getLink();
        }

        log.warn("No active tournament found for hall={}", hall);
        return null;
    }

    private boolean isHallMatch(PlayerNotification p, int hall) {
        Tournament t = p.getTournament();

        if (t.getTime() == null) return false;

        return switch (hall) {
            case 10 -> List.of("00:00", "06:00", "12:00", "18:00")
                    .contains(normalizeTime(t.getTime()));
            case 11 -> List.of("07:00", "13:00", "19:00")
                    .contains(normalizeTime(t.getTime()));
            default -> false;
        };
    }

    private PlayerNotification findCurrent(List<PlayerNotification> list) {
        LocalTime now = LocalTime.now();

        return list.stream()
                .filter(p -> p.getTournament().getTime() != null)
                .filter(p -> !p.getTournament().isFinished())
                .filter(p -> !p.getTournament().isCancelled()) // 🔥 важно
                .filter(p -> {
                    LocalTime t = LocalTime.parse(
                            normalizeTime(p.getTournament().getTime())
                    );

                    return now.isAfter(t.minusMinutes(10)) &&
                            now.isBefore(t.plusHours(2));
                })
                .findFirst()
                .orElseGet(() ->
                        list.stream()
                                .filter(p -> p.getTournament().getTime() != null)
                                .filter(p -> !p.getTournament().isFinished())
                                .filter(p -> !p.getTournament().isCancelled()) // 🔥 важно
                                .min(Comparator.comparing(p ->
                                        LocalTime.parse(
                                                normalizeTime(p.getTournament().getTime())
                                        )
                                ))
                                .orElse(null)
                );
    }

    private String normalizeTime(String time) {
        try {
            return LocalTime.parse(time).toString();
        } catch (Exception e) {
            log.warn("Failed to normalize time: {}", time);
            return time;
        }
    }
}