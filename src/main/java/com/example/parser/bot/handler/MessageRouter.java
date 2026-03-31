package com.example.parser.bot.handler;

import com.example.parser.config.AdminProperties;
import com.example.parser.formatter.LiveMatchFormatter;
import com.example.parser.formatter.StatsFormatter;
import com.example.parser.domain.dto.FullStatsDto;
import com.example.parser.domain.entity.Player;
import com.example.parser.domain.model.Match;
import com.example.parser.parser.MatchParser;
import com.example.parser.parser.TournamentParser;
import com.example.parser.service.*;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageRouter {

    private final CallbackHandler callbackHandler;
    private final TextHandler textHandler;

    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {

        if (update.hasCallbackQuery()) {
            callbackHandler.handle(update, bot);
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            textHandler.handle(update, bot);
        }
    }
}