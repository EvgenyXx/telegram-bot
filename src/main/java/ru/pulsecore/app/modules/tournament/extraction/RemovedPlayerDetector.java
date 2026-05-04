package ru.pulsecore.app.modules.tournament.extraction;

import ru.pulsecore.app.core.model.Match;
import ru.pulsecore.app.modules.tournament.calculation.MatchStage;
import ru.pulsecore.app.modules.tournament.calculation.strategy.removed.RemovedStage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;


/*
класс занимается определением стадии снятия игрока
 */
@Component
@RequiredArgsConstructor
public class RemovedPlayerDetector {

    public RemovedResult detect(String groupRemovedPlayer, List<Match> matches) {
        String removedPlayer = groupRemovedPlayer;
        RemovedStage stage = RemovedStage.NONE;

        if (removedPlayer != null && !removedPlayer.isBlank()) {
            stage = RemovedStage.GROUP;
        } else {
            removedPlayer = detectRemovedPlayerFromSemi(matches);
            if (removedPlayer != null && !removedPlayer.isBlank()) {
                stage = RemovedStage.SEMI_FINAL;
            }
        }

        return new RemovedResult(removedPlayer, stage);
    }

    private String detectRemovedPlayerFromSemi(List<Match> matches) {
        List<Match> semiMatches = matches.stream()
                .filter(m -> MatchStage.SEMI_FINAL.matches(m.getStage()))
                .toList();

        List<Match> canceledSemis = semiMatches.stream()
                .filter(this::isCanceled)
                .toList();

        Match canceledSemi = canceledSemis.stream().findFirst().orElse(null);
        if (canceledSemi == null) {
            return null;
        }

        String p1 = canceledSemi.getPlayer1();
        String p2 = canceledSemi.getPlayer2();

        String p1Norm = normalize(p1);
        String p2Norm = normalize(p2);

        Match finalMatch = matches.stream()
                .filter(m -> MatchStage.FINAL.matches(m.getStage()))
                .findFirst()
                .orElse(null);

        if (finalMatch == null) {
            return null;
        }

        String f1 = normalize(finalMatch.getPlayer1());
        String f2 = normalize(finalMatch.getPlayer2());

        if (!p1Norm.equals(f1) && !p1Norm.equals(f2)) {
            return p1;
        }

        if (!p2Norm.equals(f1) && !p2Norm.equals(f2)) {
            return p2;
        }

        return null;
    }

    private boolean isCanceled(Match m) {
        if (m.getStatus() == null) {
            return false;
        }
        String s = m.getStatus().toLowerCase();
        return s.contains("отмен") || s.contains("cancel");
    }

    private String normalize(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}