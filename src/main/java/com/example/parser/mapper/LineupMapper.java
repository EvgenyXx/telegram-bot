package com.example.parser.mapper;

import com.example.parser.domain.dto.LineupResponseDto;
import com.example.parser.domain.entity.Lineup;

import java.util.Arrays;

public class LineupMapper {

    public static LineupResponseDto toDto(Lineup lineup) {
        return LineupResponseDto.builder()
                .league(lineup.getLeague())
                .time(lineup.getTime())
                .players(Arrays.asList(lineup.getPlayers().split(", ")))
                .build();
    }
}