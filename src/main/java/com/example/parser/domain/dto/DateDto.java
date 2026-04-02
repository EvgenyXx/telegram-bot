package com.example.parser.domain.dto;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class DateDto {
    private String date;

    // getters/setters
}