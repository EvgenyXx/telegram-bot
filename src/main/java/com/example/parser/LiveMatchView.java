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
            text = buildNoLiveText(data.getLastMatch());
        }

        // ❗ не обновляем если текст тот же
        if (!shouldUpdate(chatId, text)) return;

        // 🔄 если есть messageId → редактируем
        if (messageId != null) {
            try {
                messageService.editMessage(bot, chatId, messageId, text, getKeyboard());
                return;
            } catch (Exception e) {

                // ❗ если просто "не изменилось" — игнор
                if (e.getMessage() != null && e.getMessage().contains("message is not modified")) {
                    return;
                }

                // 💥 если сообщение умерло → создаём новое
                Integer newId = sendNew(chatId, bot, text);
                liveMatchService.setMessageId(chatId, newId);
                return;
            }
        }

        // 🆕 если вообще не было сообщения
        Integer newId = sendNew(chatId, bot, text);
        liveMatchService.setMessageId(chatId, newId);
    }

    // ================== NEW MESSAGE ==================

    private Integer sendNew(Long chatId, TelegramLongPollingBot bot, String text) throws Exception {
        Message msg = messageService.sendInlineKeyboardAndGetMessage(bot, chatId, text, getKeyboard());
        return msg.getMessageId();
    }

    // ================== ТЕКСТ ==================

    private String buildLiveText(Match live) {

        return "```" +
                (System.currentTimeMillis() / 1000 % 2 == 0 ? "🔴 LIVE\n\n" : "⚫ LIVE\n\n") +
                "Стол " + live.getTable() + "\n" +
                "Лига " + live.getLeague() + "\n\n" +

                formatter.formatLine(
                        live.getPlayer1(),
                        live.getScore1(),
                        live.getSetsDetails(),
                        true
                ) + "\n" +

                formatter.formatLine(
                        live.getPlayer2(),
                        live.getScore2(),
                        live.getSetsDetails(),
                        false
                ) + "\n\n" +

                live.getStage() +
                "```";
    }

    private String buildNoLiveText(Match last) {

        if (last == null) {
            return "⏳ Сейчас нет активного матча...";
        }

        return "⏳ Сейчас нет активного матча...\n\n" +
                "Последний матч:\n\n" +
                formatSimple(last.getPlayer1(), last.getScore1()) + "\n" +
                formatSimple(last.getPlayer2(), last.getScore2());
    }

    private String formatSimple(String player, int score) {
        return String.format("%-13s %d", player, score);
    }

    // ================== KEYBOARD ==================

    private InlineKeyboardMarkup getKeyboard() {

        InlineKeyboardButton reset = new InlineKeyboardButton();
        reset.setText("🚪 Выйти из лайва");
        reset.setCallbackData("reset_live");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(List.of(reset)));

        return markup;
    }

    // ================== OPTIMIZATION ==================

    private boolean shouldUpdate(Long chatId, String newText) {

        String last = liveMatchService.getLastMessage(chatId);

        if (newText.equals(last)) {
            return false;
        }

        liveMatchService.setLastMessage(chatId, newText);
        return true;
    }

    // ================== ДЛЯ UPDATER ==================

    public Integer renderAndReturnMessageId(Long chatId,
                                            TelegramLongPollingBot bot,
                                            LiveMatchData data) throws Exception {

        String text;

        if (data.getMatch() != null) {
            text = buildLiveText(data.getMatch());
        } else {
            text = buildNoLiveText(data.getLastMatch());
        }

        Message msg = messageService.sendAndReturn(bot, chatId, text);
        return msg.getMessageId();
    }

    public void update(Long chatId,
                       TelegramLongPollingBot bot,
                       LiveMatchData data,
                       Integer messageId) throws Exception {

        String text;

        if (data.getMatch() != null) {
            text = buildLiveText(data.getMatch());
        } else {
            text = buildNoLiveText(data.getLastMatch());
        }

        messageService.editMessage(bot, chatId, messageId, text, getKeyboard());
    }
}