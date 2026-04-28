package com.example.parser.modules.player.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerProfileResponse {
    private Long id;
    private String name;
}