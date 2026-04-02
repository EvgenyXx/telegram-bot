package com.example.parser.service;

import com.example.parser.domain.dto.TournamentDto;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class TournamentMessageBuilder {

    public String build(List<TournamentDto> tournaments) {
        StringBuilder msg = new StringBuilder();

        msg.append("🔥 Ты в расписании турниров:\n\n");

        for (TournamentDto t : tournaments) {
            msg.append(formatTournament(t));
        }

        return msg.toString();
    }

    private String formatTournament(TournamentDto t) {
        StringBuilder block = new StringBuilder();

        // 📅 ДАТА + ВРЕМЯ
        if (t.getDate() != null) {
            block.append("📅 ").append(t.getDate().getDate());

//            if (t.getDate().getTime() != null) {
//                block.append(" ").append(t.getDate().getTime());
//            }

            block.append("\n");
        }

        // ЛИГА
        block.append("🏆 Лига: ").append(nullSafe(t.getLeague())).append("\n");

        // ЗАЛ
        block.append("📍 Зал: ").append(nullSafe(t.getHall())).append("\n\n");

        // СОСТАВ
        block.append("Состав:\n");

        if (t.getPlayers() != null) {
            for (String p : t.getPlayers()) {
                block.append(p).append("\n");
            }
        }

        block.append("\n──────────────\n\n");

        return block.toString();
    }

    private String nullSafe(String val) {
        return val == null ? "-" : val;
    }
}