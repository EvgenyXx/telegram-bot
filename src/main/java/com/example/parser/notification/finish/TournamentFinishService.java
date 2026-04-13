package com.example.parser.notification.finish;

import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.domain.entity.Tournament;
import com.example.parser.notification.PlayerNotificationRepository;
import com.example.parser.notification.TournamentProcessService;
import com.example.parser.tournament.ResultService;
import com.example.parser.tournament.parser.TournamentParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentFinishService {

    private final TournamentParser tournamentParser;
    private final ResultService resultService;
    private final TournamentProcessService processService;
    private final PlayerNotificationRepository repo;

    public boolean handleFinished(Tournament t,
                                  List<PlayerNotification> notifications,
                                  Document doc) throws Exception {

        if (!tournamentParser.isFinished(doc)) return false;

        ResultService.ParsedResult parsed = resultService.calculateAll(doc);

        processService.processTournament(notifications, parsed);

        t.setFinished(true);
        t.setProcessed(true);

        repo.saveAll(notifications);

        log.info("🏁 tournament finished: id={}, users={}, results={}",
                t.getExternalId(),
                notifications.size(),
                parsed.getResults().size());

        return true;
    }
}