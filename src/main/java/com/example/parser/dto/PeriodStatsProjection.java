package com.example.parser.dto;

public interface PeriodStatsProjection {

    Integer getSum();
    Double getAverage();
    Double getMinusThreePercent();
    int getCount();
}