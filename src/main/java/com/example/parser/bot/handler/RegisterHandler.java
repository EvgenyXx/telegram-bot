//package com.example.parser.bot.handler;
//
//import com.example.parser.config.AdminProperties;
//import com.example.parser.player.Player;
//import com.example.parser.notification.MessageService;
//import com.example.parser.player.PlayerService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.telegram.telegrambots.bots.TelegramLongPollingBot;
//import org.telegram.telegrambots.meta.api.objects.Update;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class RegisterHandler {
//
//    private final PlayerService playerService;
//    private final MessageService messageService;
//    private final AdminProperties adminProperties;
//
//    public void handle(Update update, TelegramLongPollingBot bot) {
//
//        String text = update.getMessage().getText();
//        Long chatId = update.getMessage().getChatId();
//        Long telegramId = update.getMessage().getFrom().getId();
//
//        log.info("📝 REGISTER INPUT: {}", text);
//
//        Player existing = playerService.getByTelegramId(telegramId);
//
//        if (existing != null) {
//            log.warn("⚠️ USER ALREADY EXISTS: {}", existing.getName());
//            messageService.send(bot, chatId,
//                    "Ты уже зарегистрирован: " + existing.getName());
//            messageService.sendMenu(bot, chatId, telegramId, null);
//            return;
//        }
//
//        if (!isValidFullName(text)) {
//            log.error("❌ INVALID NAME: {}", text);
//            messageService.send(bot, chatId,
//                    "❌ Введи имя и фамилию правильно\nпример: Иван Иванов");
//            return;
//        }
//
//        log.info("✅ SAVING USER...");
//        playerService.registerIfNotExists(telegramId, text);
//
//        log.info("📩 NOTIFY ADMINS");
//        for (Long adminId : adminProperties.getAdmins()) {
//            messageService.send(bot, adminId,
//                    "🆕 Новый пользователь:\n👤 " + text + "\n🆔 " + telegramId);
//        }
//
//        log.info("🎉 SUCCESS REGISTER");
//        messageService.send(bot, chatId,
//                "✅ Вы зарегистрированы: " + text);
//        messageService.sendMenu(bot, chatId, telegramId, null);
//    }
//
//    private boolean isValidFullName(String text) {
//        return text.matches("^[А-Яа-яЁё]+\\s[А-Яа-яЁё]+$");
//    }
//}