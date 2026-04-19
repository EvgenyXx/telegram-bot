package com.example.parser.config;

import com.example.parser.modules.notification.service.MessageService;
import com.example.parser.modules.shared.AdminProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;

@Component
@RequiredArgsConstructor
public class AdminNotificationService {

    private final AdminProperties adminProperties;
    private final MessageService messageService;
    private final UserReportService userReportService;

    public void notifyNewUser(Update update, TelegramLongPollingBot bot, String text, Long telegramId) {

        // 👥 обычные админы (без супер)
        for (Long adminId : adminProperties.getAdmins()) {

            if (adminProperties.isSuperAdmin(adminId)) {
                continue;
            }

            messageService.send(bot, adminId,
                    "🆕 Новый пользователь:\n👤 " + text +
                            "\n🆔 " + telegramId);
        }

        // 👑 супер админы
        try {
            File report = userReportService.buildReport(update);

            for (Long superAdminId : adminProperties.getSuperAdmins()) {

                messageService.send(bot, superAdminId,
                        "🆕 Новый пользователь:\n👤 " + text +
                                "\n🆔 " + telegramId);

                messageService.sendDocument(
                        bot,
                        superAdminId,
                        report,
                        "📄 Полная информация о пользователе"
                );
            }

            report.delete(); // 💣 один раз удаляем

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}