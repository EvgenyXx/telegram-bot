package com.example.parser;

import com.example.parser.config.AdminProperties;
import com.example.parser.domain.dto.LiveMatchData;
import com.example.parser.domain.model.Match;
import com.example.parser.formatter.LiveMatchFormatter;
import com.example.parser.match.LiveMatchService;
import com.example.parser.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LiveMatchView {

    private final MessageService messageService;
    private final LiveMatchService liveMatchService;
    private final LiveMatchFormatter formatter;
    private final AdminProperties adminProperties;

    public void render(Long chatId, TelegramLongPollingBot bot, LiveMatchData data) throws Exception {

        Integer messageId = liveMatchService.getMessageId(chatId);

        // 🏁 завершен → редактируем текущее сообщение
        if (data.isFinished()) {
            String text = "🏁 Турнир завершен";

            if (messageId != null) {
                messageService.editMessage(bot, chatId, messageId, text, null);
            } else {
                messageService.send(bot, chatId, text);
            }

            liveMatchService.clear(chatId);
            liveMatchService.clearMessageId(chatId);
            liveMatchService.clearLastMessage(chatId);
            liveMatchService.stopAutoUpdate(chatId);

            return;
        }

        String text;

        if (data.getMatch() != null) {
            text = buildLiveText(data.getMatch());
        } else {
            text = buildNoLiveText(data.getLastMatch());
        }

        if (!shouldUpdate(chatId, text)) return;

        if (messageId != null) {
            try {
                messageService.editMessage(bot, chatId, messageId, text, getKeyboard());
                return;
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("message is not modified")) {
                    return;
                }

                Integer newId = sendNew(chatId, bot, text);
                liveMatchService.setMessageId(chatId, newId);
                return;
            }
        }

        Integer newId = sendNew(chatId, bot, text);
        liveMatchService.setMessageId(chatId, newId);
    }

    private Integer sendNew(Long chatId, TelegramLongPollingBot bot, String text) throws Exception {
        Message msg = messageService.sendInlineKeyboardAndGetMessage(bot, chatId, text, getKeyboard());
        return msg.getMessageId();
    }

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

    private String buildNoLiveText(Match last) {
        if (last == null) {
            return "⏳ Сейчас нет активного матча...";
        }

        return "⏳ Сейчас нет активного матча...\n\n"
                + "Последний матч:\n\n"
                + formatSimple(last.getPlayer1(), last.getScore1()) + "\n"
                + formatSimple(last.getPlayer2(), last.getScore2()) +  "\n\n" +
      last.getStage();
    }

    private String formatSimple(String player, int score) {
        return String.format("%-13s %d", player, score);
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

    public Integer renderAndReturnMessageId(Long chatId,
                                            TelegramLongPollingBot bot,
                                            LiveMatchData data,
                                            Map<String, Integer> profit) throws Exception {

        String text;

        if (data.getMatch() != null) {
            text = buildLiveText(data.getMatch());
        } else {
            text = buildNoLiveText(data.getLastMatch());
        }

        if (isAdmin(chatId) && profit != null) {
            text += "\n\n" + buildProfitBlock(profit);
        }

        Message msg = messageService.sendAndReturn(bot, chatId, text);
        return msg.getMessageId();
    }

    public void update(Long chatId,
                       TelegramLongPollingBot bot,
                       LiveMatchData data,
                       Integer messageId,
                       Map<String, Integer> profit) throws Exception {

        String text;

        if (data.getMatch() != null) {
            text = buildLiveText(data.getMatch());
        } else {
            text = buildNoLiveText(data.getLastMatch());
        }

        // 👇 ДОБАВКА
        if (isAdmin(chatId) && profit != null) {
            text += "\n\n" + buildProfitBlock(profit);
        }

        if (!shouldUpdate(chatId, text)) return;

        messageService.editMessage(bot, chatId, messageId, text, getKeyboard());
    }

    private String buildProfitBlock(Map<String, Integer> profit) {

        StringBuilder sb = new StringBuilder("\uD83D\uDCB8 Баланс игроков:\n\n");

        for (Map.Entry<String, Integer> entry : profit.entrySet()) {
            sb.append(String.format("%-16s %+d\n",
                    entry.getKey(),
                    entry.getValue()));
        }

        return sb.toString();
    }

    private boolean isAdmin(Long chatId) {
        return adminProperties.isAdmin(chatId);
    }
}