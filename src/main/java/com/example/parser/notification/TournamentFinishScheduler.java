package com.example.parser.notification;

import com.example.parser.TournamentRepository;
import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.domain.entity.Tournament;
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
    private final TournamentRepository tournamentRepo;

    @Scheduled(fixedRate = 420000, initialDelay = 30000)
    public void checkFinished() {

        log.debug("checkFinished triggered");

        List<PlayerNotification> list = loadPending();

        Map<String, List<PlayerNotification>> grouped = groupByTournament(list);

        log.info("pending finished tournaments count={}", grouped.size());

        grouped.forEach(this::processTournament);
    }

    private List<PlayerNotification> loadPending() {
        return  repo.findNotFinishedWithTournament();
    }

    // 🔥 теперь группируем по link из Tournament
    private Map<String, List<PlayerNotification>> groupByTournament(List<PlayerNotification> list) {
        return list.stream()
                .filter(p -> p.getTournament() != null)
                .collect(Collectors.groupingBy(p -> p.getTournament().getLink()));
    }

    private void processTournament(String link, List<PlayerNotification> notifications) {

        try {
            if (link == null) {
                log.warn("skip tournament with null link");
                return;
            }

            PlayerNotification sample = notifications.get(0);

            if (sample.getTournament() == null) {
                log.warn("skip tournament without Tournament entity");
                return;
            }

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

    // 🔥 FIX: теперь через Tournament
    private boolean isFuture(PlayerNotification pn) {

        Tournament t = pn.getTournament();

        return t != null &&
                t.getDate() != null &&
                t.getDate().isAfter(LocalDate.now());
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

        if (notifications.isEmpty()) return;

        Tournament tournament = notifications.get(0).getTournament();
        if (tournament == null) return;

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
        }

        // 🔥 ВАЖНО: один раз после цикла
        tournament.setFinished(true);
        tournamentRepo.save(tournament);
    }
}