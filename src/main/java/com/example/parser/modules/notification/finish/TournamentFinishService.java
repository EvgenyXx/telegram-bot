package com.example.parser.modules.notification.finish;

import com.example.parser.modules.tournament.repository.TournamentRepository;
import com.example.parser.modules.notification.domain.PlayerNotification;
import com.example.parser.modules.tournament.domain.TournamentEntity;
import com.example.parser.modules.notification.repository.PlayerNotificationRepository;
import com.example.parser.modules.notification.service.TournamentProcessService;
import com.example.parser.modules.tournament.service.result.ParsedResult;
import com.example.parser.modules.tournament.service.result.ResultService;
import com.example.parser.modules.tournament.service.result.TournamentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;



@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TournamentFinishService {


    private final ResultService resultService;
    private final TournamentProcessService processService;
    private final PlayerNotificationRepository repo;
    private final TournamentRepository tournamentRepository;

    public boolean handleFinished(TournamentEntity t,
                                  List<PlayerNotification> notifications,
                                  Document doc) throws Exception {



        ParsedResult parsed = resultService.calculateAll(doc);
        if (parsed.getStatus() != TournamentStatus.FINISHED) return false;

        processService.processTournament(notifications, parsed);

        // 🔥 ВАЖНО — СТАВИМ ФЛАГИ
        t.setFinished(true);
        t.setProcessed(true);

        // 🔥 ВАЖНО — СОХРАНЯЕМ ТУРНИР
        tournamentRepository.save(t);

        // 🔥 потом уже нотификации
        repo.saveAll(notifications);

        log.info("🏁 tournament finished: id={}, users={}, results={}",
                t.getExternalId(),
                notifications.size(),
                parsed.getResults().size());

        return true;
    }
}