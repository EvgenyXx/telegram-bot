package com.example.parser.modules.notification.processor;

import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.tournament.persistence.entity.TournamentEntity;
import com.example.parser.core.integration.DocumentLoader;
import com.example.parser.modules.notification.repository.PlayerNotificationRepository;
import com.example.parser.modules.notification.start.TournamentNotificationService;
import com.example.parser.modules.notification.start.TournamentTimeService;
import com.example.parser.modules.shared.exception.SiteUnavailableException;
import com.example.parser.modules.tournament.parser.TournamentStatusParser;
import com.example.parser.modules.tournament.domain.TournamentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentProcessor {

    private final DocumentLoader documentLoader;
    private final TournamentTimeService timeService;
    private final TournamentNotificationService notificationService;
    private final PlayerNotificationRepository repo;
    private final TournamentStatusParser tournamentStatusParser;

    public void process(String link, List<PlayerNotification> notifications) {
        if (link == null || notifications.isEmpty()) return;

        try {
            TournamentEntity t = notifications.get(0).getTournament();
            if (t == null) return;

            Document doc = documentLoader.load(link);
            TournamentStatus status = tournamentStatusParser.parseStatus(doc);

            if (handleCancelled(t, notifications, status)) return;

            if (t.isStarted()) return;
            if (!timeService.isToday(t)) return;

            boolean startedByParser = status == TournamentStatus.IN_PROGRESS
                    || status == TournamentStatus.FINISHED;
            boolean startedByTime = timeService.isStartedByTime(t);

            if (!startedByParser && !startedByTime) return;

            int success = notificationService.sendStart(notifications);
            if (success > 0) {
                t.setStarted(true);
                repo.saveAll(notifications);
            }
        } catch (SiteUnavailableException e) {
            // cool-down active, skip
        } catch (Exception e) {
            log.error("failed to process tournament: link={}", link, e);
        }
    }

    private boolean handleCancelled(TournamentEntity t,
                                    List<PlayerNotification> notifications,
                                    TournamentStatus status) {
        if (status != TournamentStatus.CANCELLED) return false;
        if (t.isCancelled()) return true;

        t.setCancelled(true);
        notificationService.sendCancelled(notifications);
        repo.saveAll(notifications);
        return true;
    }
}