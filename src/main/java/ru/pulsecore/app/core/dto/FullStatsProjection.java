package ru.pulsecore.app.core.dto;

public interface FullStatsProjection {
    Long getCount();
    Double getSum();
    Double getAvg();
}