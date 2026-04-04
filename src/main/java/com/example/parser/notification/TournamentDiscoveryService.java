package com.example.parser.notification;

import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.notification.formatter.TournamentMessageBuilder;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import com.example.parser.tournament.UpcomingTournamentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

//Чем занимается:
//🔍 Находит новые турниры для пользователя, сохраняет их и отправляет уведомление
//
//Коротко:
//👉 получает турниры через API → фильтрует новые → сохраняет → шлет сообщение пользователю
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

    public void checkNewTournaments(Long telegramId) {
        Player user = getUser(telegramId);
        if (user == null) return;

        List<TournamentDto> tournaments = getTournaments(user);
        if (tournaments.isEmpty()) return;

        List<TournamentDto> newTournaments = findNewTournaments(telegramId, tournaments);
        if (newTournaments.isEmpty()) return;

        saveNotifications(telegramId, newTournaments);

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

    private List<TournamentDto> findNewTournaments(Long telegramId, List<TournamentDto> tournaments) {
        return tournaments.stream()
                .filter(t -> !notificationRepo.existsByTelegramIdAndTournamentId(telegramId, t.getId()))
                .toList();
    }

    private void saveNotifications(Long telegramId, List<TournamentDto> tournaments) {
        for (TournamentDto t : tournaments) {
            PlayerNotification pn = notificationFactory.create(telegramId, t);
            notificationRepo.save(pn);
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

//    public void checkNewTournaments(Long telegramId) {
//
//        Player user = userService.getByTelegramId(telegramId);
//        if (user == null) {
//            log.warn("User not found for telegramId={}", telegramId);
//            return;
//        }
//
//        String fullName = user.getName();
//
//        List<TournamentDto> tournaments =
//                tournamentService.findPlayerTournaments(fullName);
//
//        if (tournaments.isEmpty()) {
//            return;
//        }
//
//        StringBuilder msg = new StringBuilder();
//        boolean hasNew = false;
//
//        for (TournamentDto t : tournaments) {
//
//            boolean alreadySent =
//                    notificationRepo.existsByTelegramIdAndTournamentId(telegramId, t.getId());
//
//            if (alreadySent) {
//                continue;
//            }
//
//            hasNew = true;
//
//            msg.append(messageBuilder.build(t));
//
//            PlayerNotification pn =
//                    notificationFactory.create(telegramId, t);
//
//            notificationRepo.save(pn);
//        }
//
//        if (!hasNew) {
//            return;
//        }
//
//        // 🔥 вот здесь дергаем NotificationService
//        notificationService.sendNewTournament(
//                telegramId,
//                "🔥 Новые турниры:\n\n" + msg
//        );
//    }
}