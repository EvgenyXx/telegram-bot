package com.example.parser.formatter;

import com.example.parser.dto.ResultDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TournamentMessageFormatter {

    public String formatResults(List<ResultDto> results, double bonus) {
        StringBuilder sb = new StringBuilder();

        int i = 1;

        for (ResultDto r : results) {
            double finalAmount = r.getTotal() + bonus;

            sb.append(i++)
                    .append(". ")
                    .append(r.getPlayer())
                    .append(" — ");

            // 🔥 разная логика
            if (bonus > 0) {
                sb.append((int) r.getTotal())
                        .append(" + 🌙")
                        .append((int) bonus)
                        .append(" = ")
                        .append((int) finalAmount);
            } else {
                sb.append((int) r.getTotal());
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}