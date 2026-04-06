//package com.example.parser.bot.handler;
//
//import com.example.parser.tournament.HistoryService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.telegram.telegrambots.bots.TelegramLongPollingBot;
//
//import org.telegram.telegrambots.meta.api.objects.Update;
//
//@Component
//@RequiredArgsConstructor
//class HistoryHandler {
//
//    private final HistoryService historyService;
//
//    public void handle(Update update, TelegramLongPollingBot bot) {
//        historyService.handleHistory(update, bot);
//    }
//
//    public void handleSum(Update update, TelegramLongPollingBot bot) {
//        historyService.handleSum(update, bot);
//    }
//}