package ru.pulsecore.app.core.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultDto {

    private Long id;
    private String player;
    private int place;
    private int bonus;
    private int total;
    private String date;

}