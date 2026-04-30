package com.example.parser.modules.player.service.strategy;

import com.example.parser.core.dto.TournamentDto;
import com.example.parser.modules.player.domain.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewTournamentMailStrategy implements MailStrategy {

    private final MailProperties mailProperties;

    @Override
    public String getType() {
        return MailTypes.NEW_TOURNAMENT;
    }

    @Override
    public SimpleMailMessage createMessage(String to, Object... args) {
        TournamentDto tournament = (TournamentDto) args[0];
        Player player = (Player) args[1];

        String date = tournament.getDate() != null ? tournament.getDate().getDate() : "не указана";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailProperties.getFrom());
        message.setTo(to);
        message.setSubject("🏓 " + player.getName() + ", вы записаны на турнир — PulseCore");
        message.setText(getHtmlBody(tournament, date));

        return message;
    }

    private static String getHtmlBody(TournamentDto tournament, String date) {
        String hall = tournament.getHall() != null ? tournament.getHall() : "не указан";
        String league = tournament.getLeague() != null ? tournament.getLeague() : "—";
        String link = tournament.getLink() != null ? tournament.getLink() : "#";

        StringBuilder playersHtml = new StringBuilder();
        if (tournament.getPlayers() != null && !tournament.getPlayers().isEmpty()) {
            for (int i = 0; i < tournament.getPlayers().size(); i++) {
                playersHtml.append("<tr><td style='padding:6px 12px;border-bottom:1px solid #eee;'>")
                        .append(i + 1).append("</td><td style='padding:6px 12px;border-bottom:1px solid #eee;'>")
                        .append(tournament.getPlayers().get(i)).append("</td></tr>");
            }
        }

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head><meta charset='UTF-8'></head>\n" +
                "<body style='font-family:Arial,sans-serif;background:#f4f6fb;margin:0;padding:30px;'>\n" +
                "<div style='max-width:520px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);'>\n" +
                "  <div style='background:linear-gradient(135deg,#4f46e5,#7c3aed);padding:28px;text-align:center;'>\n" +
                "    <h2 style='color:#fff;margin:0;font-size:20px;'>🏓 Новый турнир</h2>\n" +
                "  </div>\n" +
                "  <div style='padding:28px;'>\n" +
                "    <p style='color:#333;font-size:15px;margin:0 0 20px;'>Вы записаны на турнир!</p>\n" +
                "    <table style='width:100%;border-collapse:collapse;margin-bottom:20px;'>\n" +
                "      <tr><td style='padding:8px 0;color:#666;'>📅 Дата</td><td style='padding:8px 0;color:#333;font-weight:600;'>" + date + "</td></tr>\n" +
                "      <tr><td style='padding:8px 0;color:#666;'>🏛 Зал</td><td style='padding:8px 0;color:#333;font-weight:600;'>" + hall + "</td></tr>\n" +
                "      <tr><td style='padding:8px 0;color:#666;'>🏆 Лига</td><td style='padding:8px 0;color:#333;font-weight:600;'>" + league + "</td></tr>\n" +
                "    </table>\n" +
                "    <div style='margin-bottom:20px;'>\n" +
                "      <p style='color:#333;font-weight:600;margin:0 0 10px;'>👥 Состав участников:</p>\n" +
                "      <table style='width:100%;border-collapse:collapse;'>\n" +
                playersHtml +
                "      </table>\n" +
                "    </div>\n" +
                "    <a href='" + link + "' style='display:block;text-align:center;background:linear-gradient(135deg,#4f46e5,#7c3aed);color:#fff;padding:14px;border-radius:8px;text-decoration:none;font-weight:600;'>Открыть турнир</a>\n" +
                "    <p style='text-align:center;margin-top:20px;color:#999;font-size:12px;'>PulseCore — ваш личный кабинет<br><a href='https://pulsecore-app.ru' style='color:#4f46e5;'>pulsecore-app.ru</a></p>\n" +
                "  </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }
}