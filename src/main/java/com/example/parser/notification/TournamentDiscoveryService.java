package com.example.parser.notification;

import com.example.parser.TournamentRepository;
import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.domain.entity.Tournament;
import com.example.parser.notification.formatter.TournamentMessageBuilder;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import com.example.parser.tournament.UpcomingTournamentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentDiscoveryService {

    private final PlayerService userService;
    private final UpcomingTournamentService tournamentService;
    private final PlayerNotificationRepository notificationRepo;
    private final NotificationFactory notificationFactory;
    private final NotificationService notificationService;
    private final TournamentMessageBuilder messageBuilder;
    private final TournamentRepository tournamentRepository;

    public void checkNewTournaments(Long telegramId) {
        Player user = getUser(telegramId);
        if (user == null) return;

        List<TournamentDto> tournaments = getTournaments(user);
        if (tournaments.isEmpty()) return;

        List<TournamentDto> newTournaments = findNewTournaments(user, tournaments);
        if (newTournaments.isEmpty()) return;

        saveNotifications(user, newTournaments);

        String message = buildMessage(newTournaments);
        notificationService.send(telegramId, message);
    }

    private Player getUser(Long telegramId) {
        Player user = userService.getByTelegramId(telegramId);
        if (user == null) {
            log.warn("User not found for telegramId={}", telegramId);
        }
        return user;
    }

    private List<TournamentDto> getTournaments(Player user) {
        return tournamentService.findPlayerTournaments(user.getName());
    }

    private List<TournamentDto> findNewTournaments(Player user, List<TournamentDto> tournaments) {
        return tournaments.stream()
                .filter(t -> !notificationRepo.existsByPlayerAndTournament_ExternalId(user, t.getId()))
                .toList();
    }

    private void saveNotifications(Player user, List<TournamentDto> tournaments) {
        for (TournamentDto t : tournaments) {

            Tournament tournament = tournamentRepository
                    .findByExternalId(t.getId())
                    .orElseGet(() -> {
                        Tournament newTournament = new Tournament();

                        newTournament.setExternalId(t.getId());
                        newTournament.setLink(t.getLink());

                        // 🔥 ВОТ ГЛАВНОЕ ИСПРАВЛЕНИЕ
                        fillDateTime(newTournament, t);

                        newTournament.setStarted(false);
                        newTournament.setFinished(false);

                        return tournamentRepository.save(newTournament);
                    });

            PlayerNotification pn = notificationFactory.create(user, tournament, t);
            notificationRepo.save(pn);
        }
    }

    // 🔥 ДОБАВИЛИ НОРМАЛЬНЫЙ ПАРСИНГ
    private void fillDateTime(Tournament tournament, TournamentDto t) {
        if (t.getDate() == null || t.getDate().getDate() == null) {
            return;
        }

        String raw = t.getDate().getDate();

        // дата
        if (raw.length() >= 10) {
            tournament.setDate(LocalDate.parse(raw.substring(0, 10)));
        }

        // время
        if (raw.length() >= 16) {
            tournament.setTime(raw.substring(11, 16));
        }
    }

    private String buildMessage(List<TournamentDto> tournaments) {
        String title = tournaments.size() > 1
                ? "🔥 Новые турниры:\n\n"
                : "🔥 Твой турнир:\n\n";

        StringBuilder msg = new StringBuilder(title);

        for (TournamentDto t : tournaments) {
            msg.append(messageBuilder.build(t));
        }

        return msg.toString();
    }
}