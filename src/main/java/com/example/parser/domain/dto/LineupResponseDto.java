package com.example.parser.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LineupResponseDto {
    private String league;
    private String time;
    private List<String> players;
}