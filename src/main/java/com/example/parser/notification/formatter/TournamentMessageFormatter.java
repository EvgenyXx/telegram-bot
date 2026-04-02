package com.example.parser.notification.formatter;

import com.example.parser.domain.dto.ResultDto;
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

    public String formatFinalMessage(boolean isFinished, boolean found) {

        if (!isFinished) {
            return "\n⏳ Турнир ещё не завершён.\n" +
                    "Данные показаны на текущий момент и не будут сохранены.\n" +
                    "Попробуйте снова после завершения турнира — результаты будут зафиксированы автоматически.";
        }

        if (found) {
            return "\n✅ Твой результат сохранён!";
        }

        return "\n⚠️ Ты не найден в турнире";
    }
}