package ru.pulsecore.app.modules.tournament.service;

import ru.pulsecore.app.core.dto.TournamentDto;
import ru.pulsecore.app.modules.notification.service.TournamentProcessService;
import ru.pulsecore.app.modules.player.domain.Player;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TournamentAutoAddService {

    private final TournamentSearchService tournamentSearchService;
    private final TournamentProcessService tournamentProcessService;

    @Async
    public void addRecentTournamentsForPlayer(Player player, int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        LocalDate endDate = LocalDate.now();

        List<TournamentDto> tournaments;
        try {
            tournaments = tournamentSearchService.findByDateRangeAndPlayer(startDate.toString(), endDate.toString(), player.getName());
        } catch (Exception e) {
            log.error("❌ Ошибка поиска турниров для {}: {}", player.getName(), e.getMessage());
            return;
        }

        int added = 0;
        for (TournamentDto t : tournaments) {
            try {
                tournamentProcessService.processByUrl(t.getLink(), player.getId().toString());
                added++;
            } catch (Exception e) {
                log.warn("⚠️ {} — {}", t.getLink(), e.getMessage());
            }
        }
        log.info("📊 Авто-добавлено {} турниров за {} дней для {}", added, days, player.getName());
    }
}