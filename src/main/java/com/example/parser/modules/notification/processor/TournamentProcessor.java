package com.example.parser.modules.notification.processor;

import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.tournament.domain.TournamentEntity;
import com.example.parser.core.integration.DocumentLoader;
import com.example.parser.modules.notification.repository.PlayerNotificationRepository;
import com.example.parser.modules.notification.start.TournamentNotificationService;
import com.example.parser.modules.notification.start.TournamentTimeService;
import com.example.parser.modules.tournament.parser.TournamentStatusParser;
import com.example.parser.modules.tournament.service.result.TournamentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.net.SocketTimeoutException;
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

            // ✅ один HTTP
            Document doc = documentLoader.load(link);

            // ✅ единый статус
            TournamentStatus status = tournamentStatusParser.parseStatus(doc);

            // ❌ CANCELLED
            if (handleCancelled(t, notifications, status)) return;

            if (t.isStarted()) return;
            if (!timeService.isToday(t)) return;

            boolean startedByParser = status == TournamentStatus.IN_PROGRESS
                    || status == TournamentStatus.FINISHED;

            boolean startedByTime = timeService.isStartedByTime(t);

            log.info("DEBUG start check: id={}, parser={}, time={}",
                    t.getExternalId(), startedByParser, startedByTime);

            if (!startedByParser && !startedByTime) return;

            // 🚀 START
            int success = notificationService.sendStart(notifications);

            if (success > 0) {
                t.setStarted(true);
                repo.saveAll(notifications);
            }

            log.info("🚀 tournament started: id={}, success={}",
                    t.getExternalId(), success);

        } catch (SocketTimeoutException e) {
            log.warn("⏱ timeout while loading tournament: link={}", link);
        } catch (Exception e) {
            log.error("❌ failed to process tournament: link={}", link, e);
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

        log.info("❌ tournament cancelled: id={}, users={}",
                t.getExternalId(), notifications.size());

        return true;
    }
}