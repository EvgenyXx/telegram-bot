package com.example.parser.domain.dto;

public interface PeriodStatsProjection {

    Integer getSum();
    Double getAverage();
    Double getMinusThreePercent();
    int getCount();
}