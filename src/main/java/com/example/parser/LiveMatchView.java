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

    // 🔥 ВОТ ФИКС ДЛЯ СЕТОВ
    private String formatSets(String sets) {
        if (sets == null || sets.isEmpty()) return "";

        // убираем скобки
        sets = sets.replace("(", "").replace(")", "").trim();

        // разбиваем по пробелам или запятым
        String[] parts = sets.split("[,\\s]+");

        return String.join("  ", parts); // двойной пробел для читаемости
    }

    private String buildNoLiveText(Match last) {

        if (last == null) {
            return "⏳ Сейчас нет активного матча...";
        }

        return "⏳ Сейчас нет активного матча...\n\n"
                + "Последний матч:\n\n"
                + formatLineFixed(last.getPlayer1(), last.getScore1(), last.getSetsDetails())
                + "\n"
                + formatLineFixed(last.getPlayer2(), last.getScore2(), last.getSetsDetails());
    }

    private String formatLineFixed(String player, int score, String setsRaw) {

        String sets = normalizeSets(setsRaw);

        return String.format(
                "%-18s %d   %s",
                player,
                score,
                sets
        );
    }

    private String normalizeSets(String sets) {

        if (sets == null || sets.isEmpty()) return "";

        // убираем скобки
        sets = sets.replace("(", "").replace(")", "");

        // если вдруг "1111" → разобьём по 2 цифры
        if (!sets.contains(",") && sets.length() > 2) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sets.length(); i += 2) {
                if (i + 2 <= sets.length()) {
                    sb.append(sets, i, i + 2).append(" ");
                }
            }
            return sb.toString().trim();
        }

        // нормальный кейс: через запятую
        String[] parts = sets.split(",");

        StringBuilder result = new StringBuilder();
        for (String p : parts) {
            result.append(p.trim()).append("  ");
        }

        return result.toString().trim();
    }

    // ================== LIVE ==================

    private String buildLiveText(Match live) {
        return "```"
                + (System.currentTimeMillis() / 1000 % 2 == 0 ? "🔴 LIVE\n\n" : "⚫ LIVE\n\n")
                + "Стол " + live.getTable() + "\n"
                + "Лига " + live.getLeague() + "\n\n"
                + formatter.formatLine(live.getPlayer1(), live.getScore1(), live.getSetsDetails(), true)
                + "\n"
                + formatter.formatLine(live.getPlayer2(), live.getScore2(), live.getSetsDetails(), false)
                + "\n\n"
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