package com.example.parser.modules.notification.processor;

import com.example.parser.modules.tournament.parser.TournamentStatusParser;
import com.example.parser.modules.tournament.repository.TournamentRepository;
import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.tournament.domain.TournamentEntity;
import com.example.parser.core.integration.DocumentLoader;
import com.example.parser.modules.notification.finish.TournamentFinishNotificationService;
import com.example.parser.modules.notification.finish.TournamentFinishService;
import com.example.parser.modules.notification.repository.PlayerNotificationRepository;
import com.example.parser.modules.tournament.service.result.TournamentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentFinishProcessor {

    private final DocumentLoader documentLoader;
    private final TournamentFinishService finishService;
    private final TournamentFinishNotificationService notificationService;
    private final PlayerNotificationRepository repo;
    private final TournamentRepository tournamentRepository;
    private final TournamentStatusParser tournamentStatusParser;

    public Result process(String link, List<PlayerNotification> notifications) {

        if (link == null || notifications == null || notifications.isEmpty()) return null;

        try {
            TournamentEntity t = notifications.stream()
                    .map(PlayerNotification::getTournament)
                    .findFirst()
                    .orElse(null);

            if (t.isProcessed()) return null;

            // ✅ 1 HTTP
            Document doc = documentLoader.load(link);

            // ✅ единый статус
            TournamentStatus status = tournamentStatusParser.parseStatus(doc);

            // ❌ CANCELLED
            if (handleCancelled(t, notifications, status)) {
                return new Result(false, true);
            }

            // 🏁 FINISHED
            if (finishService.handleFinished(t, notifications, doc)) {
                return new Result(true, false);
            }

            return null;

        } catch (Exception e) {
            log.error("❌ failed to process tournament: link={}", link, e);
            return null;
        }
    }

    private boolean handleCancelled(TournamentEntity t,
                                    List<PlayerNotification> notifications,
                                    TournamentStatus status) {

        if (status != TournamentStatus.CANCELLED) return false;
        if (t.isCancelled()) return true;

        t.setCancelled(true);
        t.setProcessed(true);

        // 🔥 сохраняем турнир
        tournamentRepository.save(t);

        notificationService.sendCancelled(notifications);
        repo.saveAll(notifications);

        log.info("❌ tournament cancelled: id={}, users={}",
                t.getExternalId(), notifications.size());

        return true;
    }

    public record Result(boolean finished, boolean cancelled) {}
}