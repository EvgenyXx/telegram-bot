package com.example.parser.bot.command;

import com.example.parser.notification.MessageService;
import com.example.parser.player.Player;
import com.example.parser.player.PlayerService;
import com.example.parser.tournament.ResultService;
import com.example.parser.tournament.TournamentResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class TournamentLinkCommand implements CommandHandler {

    private final PlayerService playerService;
    private final MessageService messageService;
    private final ResultService resultService;
    private final TournamentResultService tournamentResultService;

    @Override
    public boolean supports(String text, Player player) {
        return text != null && text.startsWith("http");
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        String link = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Long telegramId = update.getMessage().getFrom().getId();

        Player player = playerService.getByTelegramId(telegramId);

        if (player == null) {
            messageService.send(bot, chatId, "❌ Ты не зарегистрирован");
            return;
        }

        // 🔥 парсим
        ResultService.ParsedResult parsed = resultService.calculateAll(link);

        // 🔥 проверка — уже есть?
        boolean alreadyExists = tournamentResultService.exists(
                player,
                parsed.getTournamentId()
        );

        // 🔥 сохраняем / проверяем участие
        boolean found = tournamentResultService.processResults(
                parsed.getResults(),
                player,
                parsed.getTournamentId(),
                parsed.getNightBonus(),
                parsed.isFinished()
        );

        if (!found) {
            messageService.send(bot, chatId,
                    "ℹ️ Ссылка норм, но сохранять тут нечего 👀");
            return;
        }

        if (alreadyExists) {
            messageService.send(bot, chatId,
                    "ℹ️ Этот турнир уже был ранее сохранён");
            return;
        }

        messageService.send(bot, chatId,
                "✅ Турнир успешно добавлен в «Мои турниры»");
    }
}
