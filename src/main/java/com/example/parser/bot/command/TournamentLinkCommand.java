package com.example.parser.bot.command;

import com.example.parser.bot.handler.CommandHandler;
import com.example.parser.bot.ui.keyboard.TournamentResultEditKeyboardBuilder;
import com.example.parser.core.dto.TournamentLinkResult;
import com.example.parser.modules.notification.service.MessageService;
import com.example.parser.modules.notification.formatter.TournamentLinkMessageBuilder;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.service.PlayerService;
import com.example.parser.modules.tournament.application.TournamentLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
@RequiredArgsConstructor
@Order(10)
public class TournamentLinkCommand implements CommandHandler {

    private final PlayerService playerService;
    private final MessageService messageService;
    private final TournamentLinkService tournamentLinkService;
    private final TournamentLinkMessageBuilder buildMessage;
    private final TournamentResultEditKeyboardBuilder editKeyboardBuilder;

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

        TournamentLinkResult result = tournamentLinkService.process(link, player);

        String message = buildMessage.build(result);

        InlineKeyboardMarkup keyboard = buildKeyboard(result, player);

        send(bot, chatId, message, keyboard);
    }

    // 🔽 ВЫНЕСЕННАЯ ЛОГИКА

    private InlineKeyboardMarkup buildKeyboard(TournamentLinkResult result, Player player) {
        if (result.getParsed() == null) {
            return null;
        }

        return editKeyboardBuilder.build(
                player.getId(),
                result.getParsed().getTournamentId()
        );
    }

    private void send(TelegramLongPollingBot bot,
                      Long chatId,
                      String message,
                      InlineKeyboardMarkup keyboard) {

        if (keyboard == null) {
            messageService.send(bot, chatId, message);
        } else {
            messageService.sendInlineKeyboard(bot, chatId, message, keyboard);
        }
    }
}