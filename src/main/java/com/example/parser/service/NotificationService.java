package com.example.parser.service;

import com.example.parser.domain.dto.TournamentDto;
import com.example.parser.domain.entity.Player;
import com.example.parser.domain.entity.PlayerNotification;
import com.example.parser.repository.PlayerNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final PlayerService userService;
    private final UpcomingTournamentService tournamentService;
    private final MessageService messageService;
    private final PlayerNotificationRepository notificationRepo;

    public void notifyUser(Long telegramId, TelegramLongPollingBot bot) {

        Player user = userService.getByTelegramId(telegramId);
        String fullName = user.getName();

        System.out.println("👤 USER: " + fullName);

        List<TournamentDto> tournaments =
                tournamentService.findPlayerTournaments(fullName);

        if (tournaments.isEmpty()) {
            System.out.println("❌ NO TOURNAMENTS");
            return;
        }

        StringBuilder msg = new StringBuilder();
        boolean hasNew = false;

        for (TournamentDto t : tournaments) {

            // 💥 ВОТ ГЛАВНАЯ ПРОВЕРКА
            boolean alreadySent =
                    notificationRepo.existsByTelegramIdAndTournamentId(
                            telegramId, t.getId()
                    );

            if (alreadySent) continue;

            hasNew = true;

            msg.append("📅 ").append(t.getId()).append("\n");
            msg.append("🏆 Лига: ").append(t.getLeague()).append("\n");
            msg.append("📍 Зал: ").append(t.getHall()).append("\n\n");

            // 💾 СОХРАНЯЕМ
            PlayerNotification pn = new PlayerNotification();
            pn.setTelegramId(telegramId);
            pn.setTournamentId(t.getId());

            notificationRepo.save(pn);
        }

        if (!hasNew) {
            System.out.println("😴 NOTHING NEW");
            return;
        }

        messageService.send(bot, telegramId,
                "🔥 Новые турниры:\n\n" + msg);
    }
}