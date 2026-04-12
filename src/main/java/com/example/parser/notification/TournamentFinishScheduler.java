package com.example.parser.notification;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.integration.DocumentLoader;
import com.example.parser.notification.formatter.TournamentMessageFormatter;
import com.example.parser.player.Player;
import com.example.parser.tournament.ResultService;
import com.example.parser.tournament.TournamentResultService;
import com.example.parser.tournament.parser.TournamentParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentFinishScheduler {

    private final PlayerNotificationRepository repo;
    private final TournamentResultService tournamentResultService;
    private final NotificationService notificationService;
    private final ResultService resultService;
    private final DocumentLoader documentLoader;
    private final TournamentParser tournamentParser;
    private final TournamentMessageFormatter messageFormatter;

    @Scheduled(fixedRate = 420000, initialDelay = 30000)
    public void checkFinished() {

        log.debug("checkFinished triggered");

        List<PlayerNotification> list = loadPending();

        Map<String, List<PlayerNotification>> grouped = groupByTournament(list);

        log.info("pending finished tournaments count={}", grouped.size());

        grouped.forEach(this::processTournament);
    }

    private List<PlayerNotification> loadPending() {
        return repo.findByFinishedFalse();
    }

    private Map<String, List<PlayerNotification>> groupByTournament(List<PlayerNotification> list) {
        return list.stream()
                .collect(Collectors.groupingBy(PlayerNotification::getLink));
    }

    private void processTournament(String link, List<PlayerNotification> notifications) {

        try {
            if (link == null) {
                log.warn("skip tournament with null link");
                return;
            }

            PlayerNotification sample = notifications.get(0);

            if (isFuture(sample)) {
                return;
            }

            Document document = loadDocument(link);

            if (!isFinished(document)) {
                return;
            }

            ResultService.ParsedResult parsed = parse(document);

            notifyPlayers(notifications, parsed);

            log.info("tournament finished: id={}, users={}",
                    parsed.getTournamentId(),
                    notifications.size());

        } catch (Exception e) {
            log.error("failed to process tournament: link={}", link, e);
        }
    }

    private boolean isFuture(PlayerNotification pn) {
        return pn.getDate() != null && pn.getDate().isAfter(LocalDate.now());
    }

    private Document loadDocument(String link) throws Exception {
        return documentLoader.load(link);
    }

    private boolean isFinished(Document document) {
        return tournamentParser.isFinished(document);
    }

    private ResultService.ParsedResult parse(Document document) throws Exception {
        return resultService.calculateAll(document);
    }

    private void notifyPlayers(List<PlayerNotification> notifications,
                               ResultService.ParsedResult parsed) {

        String message = messageFormatter.format(parsed.getResults());

        for (PlayerNotification pn : notifications) {

            Player player = pn.getPlayer();

            if (player == null) continue;

            boolean found = tournamentResultService.processResults(
                    parsed.getResults(),
                    player,
                    parsed.getTournamentId(),
                    parsed.getNightBonus(),
                    true
            );

            if (!found) continue;

            Long telegramId = player.getTelegramId();

            notificationService.send(telegramId, message);

            pn.setFinished(true);
            repo.save(pn);
        }
    }
}