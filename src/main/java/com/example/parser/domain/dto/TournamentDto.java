package com.example.parser.domain.dto;

import lombok.Data;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TournamentDto {
    private Long id;
    private String title;
    private String hall;
    private String link;
    private String league;
    private DateDto date;
    private List<String> players; // ← ВОТ ТАК!
}