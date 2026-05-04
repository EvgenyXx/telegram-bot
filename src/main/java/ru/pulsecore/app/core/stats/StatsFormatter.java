package ru.pulsecore.app.core.stats;

import ru.pulsecore.app.core.dto.FullStatsDto;
import org.springframework.stereotype.Service;

@Service
public class StatsFormatter {

    public String formatFullStats(FullStatsDto stats) {

        if (stats == null) {
            return "❌ Нет данных";
        }

        return "📊 Твоя статистика:\n\n" +
                "📦 Турниров: " + stats.getCount() + "\n" +
                "💰 Общая сумма: " + (int) stats.getSum() + "\n" +
                "📈 Средний результат: " + (int) stats.getAvg();
    }
}