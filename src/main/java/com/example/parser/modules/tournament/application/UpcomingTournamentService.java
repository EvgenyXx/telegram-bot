package com.example.parser.modules.tournament.application;

import com.example.parser.core.dto.TournamentDto;
import com.example.parser.modules.lineup.client.MastersApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpcomingTournamentService {

    private final MastersApiClient apiClient;

    public List<TournamentDto> findPlayerTournaments(String searchName) {

        List<TournamentDto> result = new ArrayList<>();

        try {
            for (int i = 0; i < 2; i++) {

                String date = LocalDate.now().plusDays(i).toString();

                // ✅ получаем данные через клиент
                List<TournamentDto> tournaments =
                        apiClient.loadTournaments(date);

                for (TournamentDto t : tournaments) {

                    // если нужно — оставляем
                    t.setHallNumber(extractHallNumber(t.getHall()));

                    if (t.getPlayers() == null) continue;

                    for (String player : t.getPlayers()) {

                        if (player == null) continue;

                        if (isSamePlayer(searchName, player)) {
                            log.debug("FOUND player: {} in tournamentId={}", player, t.getId());
                            result.add(t);
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("ERROR while fetching tournaments for [{}]", searchName, e);
        }

        return result;
    }

    private Integer extractHallNumber(String hall) {
        if (hall == null) return null;
        try {
            return Integer.parseInt(hall.replaceAll("\\D+", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isSamePlayer(String n1, String n2) {
        return normalize(n1).equals(normalize(n2));
    }

    private String normalize(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}