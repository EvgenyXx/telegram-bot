package com.example.parser;

import com.example.parser.domain.dto.LiveMatchData;
import com.example.parser.domain.model.Match;
import com.example.parser.formatter.LiveMatchFormatter;
import com.example.parser.service.LiveMatchService;
import com.example.parser.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LiveMatchView {

    private final MessageService messageService;
    private final LiveMatchService liveMatchService;
    private final LiveMatchFormatter formatter;

    public void render(Long chatId, TelegramLongPollingBot bot, LiveMatchData data) throws Exception {

        Integer messageId = liveMatchService.getMessageId(chatId);

        // 🏁 завершен
        if (data.isFinished()) {
            liveMatchService.clear(chatId);
            liveMatchService.clearMessageId(chatId);
            liveMatchService.clearLastMessage(chatId);
            messageService.send(bot, chatId, "🏁 Турнир завершен");
            return;
        }

        String text;

        // 🔴 есть матч
        if (data.getMatch() != null) {
            text = buildLiveText(data.getMatch());
        } else {
            text = "⏳ Сейчас нет активного матча...";
        }

        if (!shouldUpdate(chatId, text)) return;

        if (messageId != null) {
            try {
                messageService.editMessage(bot, chatId, messageId, text, getKeyboard());
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("message is not modified")) {
                    return;
                }
                e.printStackTrace();
            }
        } else {
            Message msg = messageService.sendInlineKeyboardAndGetMessage(bot, chatId, text, getKeyboard());
            liveMatchService.setMessageId(chatId, msg.getMessageId());
        }
    }

    // ================== HELPERS ==================

    private String buildLiveText(Match live) {
        return "```"
                + (System.currentTimeMillis() / 1000 % 2 == 0 ? "🔴 LIVE\n\n" : "⚫ LIVE\n\n")
                + "Стол " + live.getTable() + "\n"
                + "Лига " + live.getLeague() + "\n\n"
                + formatter.formatLine(live.getPlayer1(), live.getScore1(), live.getSetsDetails(), true) + "\n"
                + formatter.formatLine(live.getPlayer2(), live.getScore2(), live.getSetsDetails(), false) + "\n\n"
                + live.getStage()
                + "```";
    }

    private InlineKeyboardMarkup getKeyboard() {
        InlineKeyboardButton reset = new InlineKeyboardButton();
        reset.setText("🚪 Выйти из лайва");
        reset.setCallbackData("reset_live");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(reset)));

        return markup;
    }

    private boolean shouldUpdate(Long chatId, String newText) {
        String last = liveMatchService.getLastMessage(chatId);

        if (newText.equals(last)) {
            return false;
        }

        liveMatchService.setLastMessage(chatId, newText);
        return true;
    }
}