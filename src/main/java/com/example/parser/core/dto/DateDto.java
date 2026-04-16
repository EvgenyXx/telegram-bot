package com.example.parser.core.dto;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class DateDto {
    private String date;

    private String timezone;        // ← ДОБАВЬ
    private Integer timezone_type;  // ← ДОБАВЬ


    // getters/setters
}