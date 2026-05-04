package ru.pulsecore.app.core.dto;

public interface PeriodStatsProjection {
    Double getSum();                // 🔥 было Integer
    Double getAverage();            // 🔥 теперь совпадает с SQL
    Double getMinusThreePercent();  // 🔥 теперь есть в SQL
    Long getCount();                // 🔥 лучше Long
}