package com.example.parser.notification.finish;

import com.example.parser.lineup.TournamentRepository;
import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.domain.entity.Tournament;
import com.example.parser.integration.DocumentLoader;
import com.example.parser.notification.PlayerNotificationRepository;
import com.example.parser.tournament.parser.TournamentParser;
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
    private final TournamentParser tournamentParser;
    private final TournamentFinishService finishService;
    private final TournamentFinishNotificationService notificationService;
    private final PlayerNotificationRepository repo;
    private final TournamentRepository tournamentRepository;

    public Result process(String link, List<PlayerNotification> notifications) {

        if (link == null || notifications == null || notifications.isEmpty()) return null;

        try {
            Tournament t = notifications.stream()
                    .map(PlayerNotification::getTournament)
                    .findFirst()
                    .orElse(null);

            if (t.isProcessed()) return null;

            // ✅ 1 HTTP
            Document doc = documentLoader.load(link);

            // ❌ CANCELLED
            if (handleCancelled(t, notifications, doc)) {
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

    private boolean handleCancelled(Tournament t,
                                    List<PlayerNotification> notifications,
                                    Document doc) {

        if (!tournamentParser.isCancelled(doc)) return false;
        if (t.isCancelled()) return true;

        t.setCancelled(true);
        t.setProcessed(true);

        // 🔥 ДОБАВЬ ЭТО
        tournamentRepository.save(t);

        notificationService.sendCancelled(notifications);
        repo.saveAll(notifications);

        log.info("❌ tournament cancelled: id={}, users={}",
                t.getExternalId(), notifications.size());

        return true;
    }

    // ✅ ВАЖНО: вот этот класс тебе не хватало
    public record Result(boolean finished, boolean cancelled) {}
}