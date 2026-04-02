package com.example.parser.notification.formatter;

import com.example.parser.domain.dto.TournamentDto;

public class TournamentMessageBuilder {

    public String build(TournamentDto t) {
        StringBuilder msg = new StringBuilder();

        String dateStr = "-";
        String timeStr = "-";

        if (t.getDate() != null && t.getDate().getDate() != null) {
            String raw = t.getDate().getDate();
            if (raw.length() >= 16) {
                dateStr = raw.substring(0, 10);
                timeStr = raw.substring(11, 16);
            }
        }

        msg.append("📅 ").append(dateStr).append(" ").append(timeStr).append("\n");
        msg.append("🏆 ").append(nullSafe(t.getLeague())).append("\n");
        msg.append("📍 ").append(nullSafe(t.getHall())).append("\n\n");

        if (t.getPlayers() != null && !t.getPlayers().isEmpty()) {
            msg.append("👥 Участники:\n");

            for (String p : t.getPlayers()) {
                msg.append("• ").append(p).append("\n");
            }

            msg.append("\n");
        }

        msg.append("🔗 ").append(t.getLink()).append("\n\n");

        return msg.toString();
    }

    private String nullSafe(String val) {
        return val == null ? "-" : val;
    }
}