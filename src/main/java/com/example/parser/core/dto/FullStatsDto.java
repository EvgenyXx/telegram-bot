package com.example.parser.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FullStatsDto {

    private long count;
    private double sum;
    private double avg;
}