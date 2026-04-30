package com.example.parser.modules.player.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PlayerProfileResponse {
    private String id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
}