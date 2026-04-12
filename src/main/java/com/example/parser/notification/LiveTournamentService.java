package com.example.parser.notification;

import com.example.parser.domain.entity.PlayerNotification;
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
                repository.findByDateOrderByTimeAsc(LocalDate.now());

        log.debug("Found {} tournaments for today", today.size());

        List<PlayerNotification> filtered = today.stream()
                .filter(p -> isHallMatch(p, hall))
                .toList();

        log.debug("Filtered {} tournaments for hall={}", filtered.size(), hall);

        PlayerNotification current = findCurrent(filtered);

        if (current != null) {
            log.info("Active tournament found: id={}, time={}, link={}",
                    current.getTournamentId(),
                    current.getTime(),
                    current.getLink());

            return current.getLink();
        }

        log.warn("No active tournament found for hall={}", hall);
        return null;
    }

    private boolean isHallMatch(PlayerNotification p, int hall) {
        if (p.getTime() == null) return false;

        return switch (hall) {
            case 10 -> List.of("00:00", "06:00", "12:00", "18:00")
                    .contains(normalizeTime(p.getTime()));
            case 11 -> List.of("07:00", "13:00", "19:00")
                    .contains(normalizeTime(p.getTime()));
            default -> false;
        };
    }

    private PlayerNotification findCurrent(List<PlayerNotification> list) {

        LocalTime now = LocalTime.now();
        log.debug("Current time: {}", now);

        return list.stream()
                .filter(p -> p.getTime() != null)
                .filter(p -> !p.isFinished()) // 🔥 ВОТ ФИКС
                .filter(p -> {
                    LocalTime t = LocalTime.parse(normalizeTime(p.getTime()));

                    boolean inWindow =
                            now.isAfter(t.minusMinutes(10)) &&
                                    now.isBefore(t.plusHours(2));

                    if (inWindow) {
                        log.debug("Tournament {} is ACTIVE (time={})",
                                p.getTournamentId(), t);
                    }

                    return inWindow;
                })
                .findFirst()
                .orElseGet(() -> {
                    log.debug("No active tournament, searching nearest");

                    return list.stream()
                            .filter(p -> p.getTime() != null)
                            .filter(p -> !p.isFinished()) // 🔥 И ТУТ
                            .min(Comparator.comparing(p ->
                                    LocalTime.parse(normalizeTime(p.getTime()))
                            ))
                            .map(p -> {
                                log.debug("Nearest tournament: id={}, time={}",
                                        p.getTournamentId(),
                                        p.getTime());
                                return p;
                            })
                            .orElse(null);
                });
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