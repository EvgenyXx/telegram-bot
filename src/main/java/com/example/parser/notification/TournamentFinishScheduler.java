package com.example.parser.notification;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.integration.DocumentLoader;
import com.example.parser.tournament.ResultService;
import com.example.parser.tournament.parser.TournamentParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentFinishScheduler {

    private final PlayerNotificationRepository repo;
    private final DocumentLoader documentLoader;
    private final TournamentParser tournamentParser;
    private final ResultService resultService;
    private final TournamentProcessService processService;

    @Scheduled(fixedRate = 420000, initialDelay = 30000)
    public void checkFinished() {

        List<PlayerNotification> list = repo.findNotFinishedWithTournament();

        Map<String, List<PlayerNotification>> grouped = list.stream()
                .filter(p -> p.getTournament() != null)
                .collect(Collectors.groupingBy(p -> p.getTournament().getLink()));

        grouped.forEach(this::processSafe);
    }

    private void processSafe(String link, List<PlayerNotification> notifications) {
        try {
            if (link == null) return;

            // берём турнир
            var tournament = notifications.get(0).getTournament();

            if (tournament == null) return;

            // 🔥 УЖЕ ОБРАБОТАН
            if (tournament.isFinished()) return;

            // 🔥 мусор пропускаем
            if (tournament.getDate() == null || tournament.getTime() == null) return;

            Document document = documentLoader.load(link);

            if (!tournamentParser.isFinished(document)) return;

            ResultService.ParsedResult parsed = resultService.calculateAll(document);

            // ✅ бизнес-логика
            processService.processTournament(notifications, parsed);

            // 🔥 фиксируем ОДИН РАЗ
            tournament.setFinished(true);

            log.info("🏁 tournament finished: id={}, link={}, users={}",
                    tournament.getExternalId(),
                    link,
                    notifications.size());

        } catch (Exception e) {
            log.error("failed to process tournament: link={}", link, e);
        }
    }
}