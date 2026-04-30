package com.example.parser.modules.player.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerResponse {
    private String id;
    private String name;
    private String email;
}