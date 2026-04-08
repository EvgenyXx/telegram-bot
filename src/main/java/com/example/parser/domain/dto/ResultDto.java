package com.example.parser.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultDto {

    private String player;
    private int place;
    private int bonus;
    private int total;
    private String date;

    public ResultDto(String player, int place, int bonus, int total,String date) {
        this.player = player;
        this.place = place;
        this.bonus = bonus;
        this.total = total;
        this.date = date;
    }


}