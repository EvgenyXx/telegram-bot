package com.example.parser.modules.notification.processor;

import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.tournament.domain.Tournament;
import com.example.parser.core.integration.DocumentLoader;
import com.example.parser.modules.notification.repository.PlayerNotificationRepository;
import com.example.parser.modules.notification.start.TournamentNotificationService;
import com.example.parser.modules.notification.start.TournamentTimeService;
import com.example.parser.modules.tournament.parser.TournamentParser;
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
    private final TournamentParser tournamentParser;
    private final TournamentTimeService timeService;
    private final TournamentNotificationService notificationService;
    private final PlayerNotificationRepository repo;

    public void process(String link, List<PlayerNotification> notifications) {
        if (link == null || notifications.isEmpty()) return;

        try {
            Tournament t = notifications.get(0).getTournament();
            if (t == null) return;

            // ✅ ЕДИНСТВЕННЫЙ HTTP
            Document doc = documentLoader.load(link);

            // ❌ CANCELLED
            if (handleCancelled(t, notifications, doc)) return;

            if (t.isStarted()) return;
            if (!timeService.isToday(t)) return;

            // ✅ БЕЗ HTTP
            boolean startedByParser = tournamentParser.isTournamentStarted(doc);
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

            // ✅ ВОТ ГЛАВНОЕ ИЗМЕНЕНИЕ
            log.warn("⏱ timeout while loading tournament: link={}", link);

        } catch (Exception e) {

            log.error("❌ failed to process tournament: link={}", link, e);
        }
    }

    private boolean handleCancelled(Tournament t,
                                    List<PlayerNotification> notifications,
                                    Document doc) {

        if (!tournamentParser.isCancelled(doc)) return false;
        if (t.isCancelled()) return true;

        t.setCancelled(true);
        notificationService.sendCancelled(notifications);
        repo.saveAll(notifications);

        log.info("❌ tournament cancelled: id={}, users={}",
                t.getExternalId(), notifications.size());

        return true;
    }
}