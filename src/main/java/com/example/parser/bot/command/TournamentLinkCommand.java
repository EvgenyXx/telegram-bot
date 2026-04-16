package com.example.parser.bot.command;


import com.example.parser.bot.keyboard.TournamentResultEditKeyboardBuilder;
import com.example.parser.core.dto.TournamentLinkResult;
import com.example.parser.modules.notification.service.MessageService;
import com.example.parser.modules.notification.formatter.TournamentLinkMessageBuilder;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.player.service.PlayerService;
import com.example.parser.modules.tournament.service.TournamentLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;




@Component
@RequiredArgsConstructor
@Order(10)//todo переделать
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

        // ✅ ВСЯ ЛОГИКА ТУТ
        TournamentLinkResult result = tournamentLinkService.process(link, player);

        // ✅ дальше только UI
        String message = buildMessage.build(result);

        InlineKeyboardMarkup keyboard = editKeyboardBuilder.build(
                player.getId(),
                result.getParsed().getTournamentId()
        );

        messageService.sendInlineKeyboard(
                bot,
                chatId,
                message,
                keyboard
        );
    }


}
