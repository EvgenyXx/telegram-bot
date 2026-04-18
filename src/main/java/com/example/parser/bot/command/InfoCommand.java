package com.example.parser.bot.command;

import com.example.parser.bot.handler.CommandHandler;
import com.example.parser.modules.notification.service.MessageService;
import com.example.parser.modules.player.domain.Player;
import com.example.parser.modules.shared.InfoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
@Order
@RequiredArgsConstructor
public class InfoCommand implements CommandHandler {

    private final InfoProperties infoProperties;
    private final MessageService messageService;

    private final String INFO = "/info";

    @Override
    public boolean supports(String text, Player player) {
        return INFO.equals(text);
    }

    @Override
    public void handle(Update update, TelegramLongPollingBot bot) throws Exception {
        Long chatId = update.getMessage().getChatId();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("🌐 Открыть сайт");
        button.setUrl(infoProperties.getUrl());

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(List.of(List.of(button)));

        messageService.sendInlineKeyboard(
                bot,
                chatId,
                "MASTERS",
                keyboard
        );
    }
}
